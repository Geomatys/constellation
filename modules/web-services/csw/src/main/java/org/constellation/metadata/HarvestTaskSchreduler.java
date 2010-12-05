/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.metadata;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.constellation.configuration.HarvestTask;
import org.constellation.configuration.HarvestTasks;
import org.constellation.metadata.harvest.CatalogueHarvester;
import org.constellation.metadata.utils.MailSendingUtilities;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.xml.MarshallerPool;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class HarvestTaskSchreduler {

    /**
     * use for debugging purpose
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.metadata");
    
    /**
     * The name of the harvest task file
     */
    private static final String HARVEST_TASK_FILE_NAME =  "HarvestTask.xml";

     /**
     * A unMarshaller to get object from harvested resource.
     */
    private final MarshallerPool marshallerPool;

    private final File configDir;

    /**
     * A list of schreduled Task (used in close method).
     */
    private final List<Timer> schreduledTask = new ArrayList<Timer>();

    private final CatalogueHarvester catalogueHarvester;

    public HarvestTaskSchreduler(File configDir, CatalogueHarvester catalogueHarvester) {
        MarshallerPool candidate = null;
        try {
            candidate = new MarshallerPool(HarvestTasks.class);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        this.marshallerPool     = candidate;
        this.configDir          = configDir;
        this.catalogueHarvester = catalogueHarvester;
        initializeHarvestTask();
    }

    /**
     * Restore all the periodic Harvest task from the configuration file "HarvestTask.xml".
     *
     * @param configDirectory The configuration directory containing the file "HarvestTask.xml"
     */
    private void initializeHarvestTask() {
        Unmarshaller unmarshaller = null;
        try {
            // we get the saved harvest task file
            final File f = new File(configDir, HARVEST_TASK_FILE_NAME);
            if (f.exists()) {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                final Object obj = unmarshaller.unmarshal(f);
                final Timer t = new Timer();
                if (obj instanceof HarvestTasks) {
                    final HarvestTasks tasks = (HarvestTasks) obj;
                    for (HarvestTask task : tasks.getTask()) {
                        final AsynchronousHarvestTask at = new AsynchronousHarvestTask(task.getSourceURL(),
                                                                                       task.getResourceType(),
                                                                                       task.getMode(),
                                                                                       task.getEmails());
                        //we look for the time passed since the last harvest
                        final long time = System.currentTimeMillis() - task.getLastHarvest();

                        long delay = 2000;
                        if (time < task.getPeriod()) {
                            delay = task.getPeriod() - time;
                        }

                        t.scheduleAtFixedRate(at, delay, task.getPeriod());
                        schreduledTask.add(t);
                    }
                } else {
                    LOGGER.severe("Bad data type for file HarvestTask.xml");
                }
            } else {
                LOGGER.info("no Harvest task found (optionnal)");
            }

        } catch (JAXBException e) {
            LOGGER.info("JAXB Exception while unmarshalling the file HarvestTask.xml");

        } finally {
            if (unmarshaller != null) {
                marshallerPool.release(unmarshaller);
            }
        }
    }



    /**
     * Save a periodic Harvest task into the specific configuration file "HarvestTask.xml".
     * This is made in order to restore the task when the server is shutdown and then restart.
     *
     * @param sourceURL  The URL of the source to harvest.
     * @param resourceType The type of the resource.
     * @param mode The type of the source: 0 for a single record (ex: an xml file) 1 for a CSW service.
     * @param emails A list of mail addresses to contact when the Harvest is done.
     * @param period The time between each Harvest.
     * @param lastHarvest The time of the last task launch.
     */
    private void saveSchreduledHarvestTask(String sourceURL, String resourceType, int mode, List<String> emails, long period, long lastHarvest) {
        final HarvestTask newTask = new HarvestTask(sourceURL, resourceType, mode, emails, period, lastHarvest);
        final File f              = new File(configDir, HARVEST_TASK_FILE_NAME);
        Marshaller marshaller     = null;
        Unmarshaller unmarshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            if (f.exists()) {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                final Object obj   = unmarshaller.unmarshal(f);
                if (obj instanceof HarvestTasks) {
                    final HarvestTasks tasks = (HarvestTasks) obj;
                    tasks.addTask(newTask);
                    marshaller.marshal(tasks, f);
                } else {
                    LOGGER.severe("Bad data type for file HarvestTask.xml");
                }
            } else {

                final boolean created = f.createNewFile();
                if (!created) {
                    LOGGER.severe("unable to create a file HarevestTask.xml");
                } else {
                    final HarvestTasks tasks = new HarvestTasks(Arrays.asList(newTask));
                    marshaller.marshal(tasks, f);
                }
            }

        } catch (IOException ex) {
            LOGGER.severe("unable to create a file for schreduled harvest task");
        } catch (JAXBException ex) {
            LOGGER.severe("A JAXB exception occurs when trying to marshall the shreduled harvest task");
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            if (unmarshaller != null) {
                marshallerPool.release(unmarshaller);
            }
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
    }

     /**
     * Update the Harvest task file by recording the last Harvest date of a task.
     * This is made in order to avoid the systematic launch of all the task when the CSW start.
     *
     * @param sourceURL Used as the task identifier.
     * @param lastHarvest a long representing the last time where the task was launch
     */
     private void updateSchreduledHarvestTask(String sourceURL, long lastHarvest) {
        final File f              = new File(configDir, HARVEST_TASK_FILE_NAME);
        Marshaller marshaller     = null;
        Unmarshaller unmarshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            if (f.exists()) {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                final Object obj   = unmarshaller.unmarshal(f);
                if (obj instanceof HarvestTasks) {
                    final HarvestTasks tasks = (HarvestTasks) obj;
                    final HarvestTask task   = tasks.getTaskFromSource(sourceURL);
                    if (task != null) {
                        task.setLastHarvest(lastHarvest);
                        marshaller.marshal(tasks, f);
                    }
                } else {
                    LOGGER.severe("Bad data type for file HarvestTask.xml");
                }
            } else {
                LOGGER.severe("There is no Harvest task file to update");
            }

        } catch (JAXBException ex) {
            LOGGER.severe("A JAXB exception occurs when trying to marshall the shreduled harvest task (update)");
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            if (unmarshaller != null) {
                marshallerPool.release(unmarshaller);
            }
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
    }

    public void newAsynchronousHarvestTask(long period, String sourceURL, String resourceType, int mode, List<String> emails) {
        final Timer t = new Timer();
        final TimerTask harvestTask = new AsynchronousHarvestTask(sourceURL, resourceType, mode, emails);
        //we launch only once the harvest
        if (period == 0) {
            t.schedule(harvestTask, 1000);

            //we launch the harvest periodically
        } else {
            t.scheduleAtFixedRate(harvestTask, 1000, period);
            schreduledTask.add(t);
            saveSchreduledHarvestTask(sourceURL, resourceType, mode, emails, period, System.currentTimeMillis() + 1000);
        }
    }

    public void destroy() {
        for (Timer t : schreduledTask) {
            t.cancel();
        }
    }

    /**
     * A task launching an harvest periodically.
     */
    class AsynchronousHarvestTask extends TimerTask {

        /**
         * The harvest mode SINGLE(0) or CATALOGUE(1)
         */
        private final int mode;

        /**
         * The URL of the data source.
         */
        private final String sourceURL;

        /**
         * The type of the resource (for single mode).
         */
        private final String resourceType;

        /**
         * A list of email addresses.
         */
        private final String[] emails;

        /**
         * Build a new Timer which will Harvest the source periodically.
         *
         */
        public AsynchronousHarvestTask(String sourceURL, String resourceType, int mode, List<String> emails) {
            this.sourceURL    = sourceURL;
            this.mode         = mode;
            this.resourceType = resourceType;
            if (emails != null) {
                this.emails = emails.toArray(new String[emails.size()]);
            } else {
                this.emails = new String[0];
            }
        }

        /**
         * This method is launch when the timer expire.
         */
        @Override
        public void run() {
            LOGGER.log(Level.INFO, "launching harvest on:{0}", sourceURL);
            try {
                int[] results;
                if (mode == 0) {
                    results = catalogueHarvester.harvestSingle(sourceURL, resourceType);
                } else {
                    results = catalogueHarvester.harvestCatalogue(sourceURL);
                }

                updateSchreduledHarvestTask(sourceURL, System.currentTimeMillis());
                /*

                 TODO does we have to send a HarvestResponseType or a custom report to the mails addresses?

                 TransactionSummaryType summary = new TransactionSummaryType(results[0],
                                                                            results[1],
                                                                            results[2], null);
                 TransactionResponseType transactionResponse = new TransactionResponseType(summary, null, "2.0.2");
                 HarvestResponseType response = new HarvestResponseType(transactionResponse);
                 */

                 final StringBuilder report = new StringBuilder("Harvest report:\n");
                 report.append("From: ").append(sourceURL).append('\n');
                 report.append("Inserted: ").append(results[0]).append('\n');
                 report.append("Updated: ").append(results[1]).append('\n');
                 report.append("Deleted: ").append(results[2]).append('\n');
                 report.append("at ").append(new Date(System.currentTimeMillis()));

                 MailSendingUtilities.mail(emails, "Harvest report", report.toString());
            } catch (SQLException ex) {
                LOGGER.severe("The service has throw an SQLException: " + ex.getMessage());
            } catch (JAXBException ex) {
                LOGGER.severe("The resource can not be parsed: " + ex.getMessage());
            } catch (MalformedURLException ex) {
                LOGGER.severe("The source URL is malformed");
            } catch (IOException ex) {
                LOGGER.severe("The service can't open the connection to the source");
            } catch (CstlServiceException ex) {
                LOGGER.severe("Constellation exception:" + ex.getMessage());
            } catch (MessagingException ex) {
                LOGGER.severe("MessagingException exception:" + ex.getMessage());
            } catch (NamingException ex) {
                LOGGER.severe("Naming exception:" + ex.getMessage());
            }
        }
    }
}
