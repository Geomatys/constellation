package org.constellation.coverage;

import org.constellation.admin.EmbeddedDatabase;
import org.constellation.admin.dao.Session;
import org.constellation.admin.dao.TaskRecord;
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.ProcessListener;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listener for Pyramidal process loaded via {@link PyramidCoverageHelper}
 *
 * @author bgarcia
 * @version 0.9
 * @since 0.9
 *
 */
public class PyramidCoverageProcessListener implements ProcessListener {

    private static final Logger LOGGER = Logger.getLogger(PyramidCoverageProcessListener.class.getName());

    private TaskRecord pyramidTask;

    @Override
    public void started(final ProcessEvent processEvent) {
        //Create task on database (state : pending)
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            String uuidTask = UUID.randomUUID().toString();
            pyramidTask = session.writeTask(uuidTask, "pyramid", null);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Unable to save task", e);
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public void progressing(final ProcessEvent processEvent) {
        LOGGER.log(Level.FINEST, "nothing when fire processing");
    }

    @Override
    public void paused(final ProcessEvent processEvent) {
        LOGGER.log(Level.FINEST, "nothing when fire paused");
    }

    @Override
    public void resumed(final ProcessEvent processEvent) {
        LOGGER.log(Level.FINEST, "nothing when fire resumed");
    }

    @Override
    public void completed(final ProcessEvent processEvent) {
        //Update state (pass to completed) on database
        try {
            pyramidTask.setState(TaskRecord.TaskState.SUCCEED);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when loading database", e);
        }
    }

    @Override
    public void failed(final ProcessEvent processEvent) {
        //Update state (pass to failed) on database
        try {
            pyramidTask.setState(TaskRecord.TaskState.FAILED);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when loading database", e);
        }
    }
}
