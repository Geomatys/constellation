package org.constellation.rest.api;

import org.constellation.engine.register.Domain;

public class LinkedDomain extends Domain {
        private boolean linked;
        private boolean canPublish;

        public LinkedDomain(Domain key, boolean linked, boolean canPublish) {
            super(key.getId(), key.getName(), key.getDescription(), key.isSystem());
            this.linked = linked;
            this.setCanPublish(canPublish);
        }

        public boolean isLinked() {
            return linked;
        }

        public void setLinked(boolean linked) {
            this.linked = linked;
        }

        public boolean isCanPublish() {
            return canPublish;
        }

        public void setCanPublish(boolean canPublish) {
            this.canPublish = canPublish;
        }
    }