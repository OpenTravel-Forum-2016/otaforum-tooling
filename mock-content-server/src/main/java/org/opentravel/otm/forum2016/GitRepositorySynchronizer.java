/**
 * Copyright (C) 2016 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.otm.forum2016;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Session;

/**
 * Handles the synchronization of content from a remote Git repository to a
 * folder location on the local file system.
 * 
 * @author S. Livezey
 */
public class GitRepositorySynchronizer implements AutoCloseable {
	
    private static final Logger log = LoggerFactory.getLogger( GitRepositorySynchronizer.class );
    
	private String repositoryUrl;
	private String branch;
	private File localRepository;
	private File configFolder;
	private Repository gitRepo;
	
	/**
	 * Constructor that specifies the URL of the remote Git repository and the local
	 * file system location of its clone.
	 * 
	 * @param repositoryUrl  the URL of the remote Git repository
	 * @param localRepository  the local folder location to which the repository is to be cloned
	 * @throws IOException  thrown if the local repository cannot be created
	 */
	public GitRepositorySynchronizer(String repositoryUrl, File localRepository) throws IOException {
		this( repositoryUrl, null, localRepository );
	}
	
	/**
	 * Constructor that specifies the URL of the remote Git repository and the local
	 * file system location of its clone.
	 * 
	 * @param repositoryUrl  the URL of the remote Git repository
	 * @param branch  the branch to be synchronized from the remote repository
	 * @param localRepository  the local folder location to which the repository is to be cloned
	 * @throws IOException  thrown if the local repository cannot be created
	 */
	public GitRepositorySynchronizer(String repositoryUrl, String branch, File localRepository) throws IOException {
		this.repositoryUrl = repositoryUrl;
		this.localRepository = localRepository;
		this.configFolder = new File( localRepository, "/.git" );
		this.branch = (branch == null) ? "master" : branch;
		
		if (!localRepository.exists()) {
			if (!localRepository.mkdirs()) {
				throw new IOException("Unable to create local directory for Git repository clone.");
			}
		}
		this.gitRepo = FileRepositoryBuilder.create( configFolder );
	}
	
	/**
	 * Synchronizes all content from the remote repository.  If the repository does
	 * not yet exist on the local file system, it is cloned from the remote repsitory
	 * URL.  If a clone has already been created, all local changes will be discarded
	 * and managed files updated with their most recent versions from the remote
	 * repository.
	 * 
	 * @throws IOException  thrown if the local repository cannot be synchronized
	 */
	public void synchronizeContent() throws IOException {
		boolean repositoryExists = false;
		
		if (isRepository()) {
			if (!(repositoryExists = isValidRepository())) {
				deleteLocalRepository( localRepository );
			}
		}
		
		if (!repositoryExists) { // create a clone
			try {
				log.info("Cloning remote Git repository - " + repositoryUrl );
				Git.cloneRepository()
						.setURI( repositoryUrl )
						.setDirectory( localRepository )
						.setTransportConfigCallback( getTransportConfigCallback() )
						.setBranch( branch ).call();
				
			} catch (GitAPIException e) {
				throw new IOException( "Error cloning Git repository.", e );
			}
			
		} else { // refresh the existing clone
			try (Git git = new Git( gitRepo )) {
				log.info("Refreshing contents of local Git repository.");
				git.reset().setMode( ResetType.HARD ).call();
				git.pull().setTransportConfigCallback( getTransportConfigCallback() ).call();
				
			} catch (GitAPIException e) {
				throw new IOException( "Error cloning Git repository.", e );
			}
		}
	}
	
	/**
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		if (gitRepo != null) {
			try {
				gitRepo.close();
				gitRepo = null;
				
			} catch (Throwable t) {}
		}
	}
	
	/**
	 * Returns true if the repsitory folder contains a Git repository.
	 * 
	 * <p>Note that a positive result from this method does not necessarily
	 * indicate that the repository is a valid clone.
	 * 
	 * @return boolean
	 */
	private boolean isRepository() {
		return (configFolder.exists() &&
				RepositoryCache.FileKey.isGitRepository( configFolder, FS.DETECTED ) );
	}
	
	/**
	 * Returns true if the repository folder contains a valid Git repository that
	 * has been successfully cloned from its remote source.
	 * 
	 * @return boolean
	 */
	private boolean isValidRepository() {
		boolean isValid = false;
		
		// The repository is a valid clone if it returns at least one
		// non-null reference
		for (Ref ref : gitRepo.getAllRefs().values()) {
			if (isValid = (ref.getObjectId() != null)) {
				break;
			}
		}
		return isValid;
	}
	
	/**
	 * Recursively deletes the contents of the given repository folder.
	 * 
	 * @param repositoryFolder  the local folder location of the repository
	 */
	private void deleteLocalRepository(File repositoryFolder) {
		if (repositoryFolder.exists()) {
			if (repositoryFolder.isDirectory()) {
				for (File folderItem : repositoryFolder.listFiles()) {
					deleteLocalRepository( folderItem );
				}
			}
			repositoryFolder.delete();
		}
	}
	
	/**
	 * Returns a <code>TransportConfigCallback</code> for SSL communications when
	 * accessing the remote Git repository.
	 * 
	 * @return TransportConfigCallback
	 */
	private TransportConfigCallback getTransportConfigCallback() { 
        final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() { 
            protected void configure(OpenSshConfig.Host host, Session session) {} 
        }; 
        return new TransportConfigCallback() { 
            public void configure(Transport transport) { 
                if (transport instanceof SshTransport) {
                    ((SshTransport) transport).setSshSessionFactory( sshSessionFactory ); 
                }
            } 
        }; 
    }
	
}
