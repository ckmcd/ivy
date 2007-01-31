/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.ivy.ant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.url.CredentialsStore;
import org.apache.ivy.util.url.URLHandler;
import org.apache.ivy.util.url.URLHandlerDispatcher;
import org.apache.ivy.util.url.URLHandlerRegistry;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Property;


/**
 * Configure Ivy with an ivyconf.xml file
 * 
 * @author Xavier Hanin
 *
 */
public class IvyConfigure extends IvyTask {
    public static class Credentials {
    	private String _realm;
    	private String _host;
    	private String _username;
    	private String _passwd;
    	
        public String getPasswd() {
            return _passwd;
        }
        public void setPasswd(String passwd) {
            _passwd = passwd;
        }
        public String getRealm() {
            return _realm;
        }
        public void setRealm(String realm) {
            _realm = format(realm);
        }
        public String getHost() {
            return _host;
        }
        public void setHost(String host) {
            _host = format(host);
        }
        public String getUsername() {
            return _username;
        }
        public void setUsername(String userName) {
            _username = format(userName);
        }
	}

	private File _file = null; 
    private URL _url = null;
    private String _realm = null;
    private String _host = null;
    private String _userName = null;
    private String _passwd = null;

    public File getFile() {
        return _file;
    }
    public void setFile(File conf) {
        _file = conf;
    }
    public URL getUrl() {
        return _url;
    }    
    public void setUrl(String url) throws MalformedURLException {
        _url = new URL(url);
    }
    public String getPasswd() {
        return _passwd;
    }
    public void setPasswd(String passwd) {
        _passwd = passwd;
    }
    public String getRealm() {
        return _realm;
    }
    public void setRealm(String realm) {
        _realm = format(realm);
    }
    public String getHost() {
        return _host;
    }
    public void setHost(String host) {
        _host = format(host);
    }
    public String getUsername() {
        return _userName;
    }
    public void setUsername(String userName) {
        _userName = format(userName);
    }
    private static String format(String str) {
        return str == null ? str : (str.trim().length() == 0 ? null : str.trim());
    }
    
    public void addConfiguredCredentials(Credentials  c) {
    	CredentialsStore.INSTANCE.addCredentials(c.getRealm(), c.getHost(), c.getUsername(), c.getPasswd());
    }

    public void execute() throws BuildException {
        try {
	        loadDefaultProperties();
        } catch (Exception ex) {
            throw new BuildException("impossible to load ivy default properties file: "+ex, ex);
        }
        ensureMessageInitialised();
        Ivy ivy = Ivy.newInstance();
        try {
            configureURLHandler();
            IvySettings settings = ivy.getSettings();
			settings.addAllVariables(getProject().getProperties());
            if (_file == null && _url == null) {
                _file = new File(getProject().getBaseDir(), getProject().getProperty("ivy.conf.file"));
                Message.verbose("searching ivyconf file: trying "+_file);
                if (!_file.exists()) {
                    _file = new File(getProject().getProperty("ivy.conf.file"));
                    Message.verbose("searching ivyconf file: trying "+_file);
                    if (!_file.exists()) {
                        Message.info("no configuration file found, using default...");
                        _file = null;
                        _url = IvySettings.getDefaultConfigurationURL();
                    }
                }
            } 
            if (_file != null) {
                if (!_file.exists()) {
                    throw new BuildException("configuration file does not exist: "+_file);
                } else {
                    ivy.configure(_file);
                }
            } else {
                ivy.configure(_url);
            }
            setIvyInstance(ivy);
        } catch (Exception ex) {
            throw new BuildException("impossible to configure ivy with given "+(_file != null ? "file: "+_file : "url :"+_url)+" :"+ex, ex);
        }
    }

    private void loadDefaultProperties() {
        Property prop = new Property() {
            public void execute() throws BuildException {
                URL url = IvySettings.getDefaultPropertiesURL();
                // this is copy of loadURL code from ant Property task  (not available in 1.5.1)
                Properties props = new Properties();
                Message.verbose("Loading " + url);
                try {
                    InputStream is = url.openStream();
                    try {
                        props.load(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                    addProperties(props);
                } catch (IOException ex) {
                    throw new BuildException(ex, getLocation());
                }
            }
        };
        prop.setProject(getProject());
        prop.execute();
    }
    
    private void configureURLHandler() {
    	CredentialsStore.INSTANCE.addCredentials(getRealm(), getHost(), getUsername(), getPasswd());

    	URLHandlerDispatcher dispatcher = new URLHandlerDispatcher();
        URLHandler httpHandler = URLHandlerRegistry.getHttp();
        dispatcher.setDownloader("http", httpHandler);
        dispatcher.setDownloader("https", httpHandler);
        URLHandlerRegistry.setDefault(dispatcher);
    }
}
