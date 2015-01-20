package org.constellation.services.component;

import java.io.File;

import org.constellation.configuration.ConfigDirectory;
import org.constellation.engine.security.WorkspaceService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CstlWorkspaceService implements WorkspaceService {

    @Override
    public File getUploadDirectory() {
        return ConfigDirectory.getUploadDirectory(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    
    
}
