/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;

import net.java.sip.communicator.launcher.ChangeJVMFrame;
//import net.java.sip.communicator.util.ScStdOut;


import org.kohsuke.args4j.*;

/**
 * 
 * @author Thomas Kuntz
 * 
 * This class contains the Main method used to launch jitsi-hammer.
 * A lot of code is copied from the SIPCommunicator.java Main
 * method, because jitsi-hammer use a lot of the same configuration that Jitsi.
 */
public class Main
{
    /**
     * Legacy home directory names that we can use if current dir name is the
     * currently active name (overridableDirName).
     */
    private static final String[] LEGACY_DIR_NAMES
        = { ".sip-communicator", "SIP Communicator" };

    /**
     * The name of the property that stores the home dir for cache data, such
     * as avatars and spelling dictionaries.
     */
    public static final String PNAME_SC_CACHE_DIR_LOCATION =
            "net.java.sip.communicator.SC_CACHE_DIR_LOCATION";

    /**
     * The name of the property that stores the home dir for application log
     * files (not history).
     */
    public static final String PNAME_SC_LOG_DIR_LOCATION =
            "net.java.sip.communicator.SC_LOG_DIR_LOCATION";

    /**
     * Name of the possible configuration file names (used under macosx).
     */
    private static final String[] LEGACY_CONFIGURATION_FILE_NAMES
        = {
            "sip-communicator.properties",
            "jitsi.properties",
            "sip-communicator.xml",
            "jitsi.xml"
        };

    /**
     * The currently active name.
     */
    private static final String OVERRIDABLE_DIR_NAME = "Jitsi";

    /**
     * The name of the property that stores our home dir location.
     */
    public static final String PNAME_SC_HOME_DIR_LOCATION
        = "net.java.sip.communicator.SC_HOME_DIR_LOCATION";

    /**
     * The name of the property that stores our home dir name.
     */
    public static final String PNAME_SC_HOME_DIR_NAME
        = "net.java.sip.communicator.SC_HOME_DIR_NAME";

    /**
     * Sets the system properties net.java.sip.communicator.SC_HOME_DIR_LOCATION
     * and net.java.sip.communicator.SC_HOME_DIR_NAME (if they aren't already
     * set) in accord with the OS conventions specified by the name of the OS.
     *
     * Please leave the access modifier as package (default) to allow launch-
     * wrappers to call it.
     *
     * @param osName the name of the OS according to which the SC_HOME_DIR_*
     * properties are to be set
     */
    static void setScHomeDir(String osName)
    {
        /*
         * Though we'll be setting the SC_HOME_DIR_* property values depending
         * on the OS running the application, we have to make sure we are
         * compatible with earlier releases i.e. use
         * ${user.home}/.sip-communicator if it exists (and the new path isn't
         * already in use).
         */
        String profileLocation = System.getProperty(PNAME_SC_HOME_DIR_LOCATION);
        String cacheLocation = System.getProperty(PNAME_SC_CACHE_DIR_LOCATION);
        String logLocation = System.getProperty(PNAME_SC_LOG_DIR_LOCATION);
        String name = System.getProperty(PNAME_SC_HOME_DIR_NAME);

        boolean isHomeDirnameForced = name != null;

        if (profileLocation == null
            || cacheLocation == null
            || logLocation == null
            || name == null)
        {
            String defaultLocation = System.getProperty("user.home");
            String defaultName = ".jitsi";

            // Whether we should check legacy names
            // 1) when such name is not forced we check
            // 2) if such is forced and is the overridableDirName check it
            //      (the later is the case with name transition SIP Communicator
            //      -> Jitsi, check them only for Jitsi)
            boolean chekLegacyDirNames
                = (name == null) || name.equals(OVERRIDABLE_DIR_NAME);

            if (osName.startsWith("Mac"))
            {
                if (profileLocation == null)
                    profileLocation =
                            System.getProperty("user.home") + File.separator
                            + "Library" + File.separator
                            + "Application Support";
                if (cacheLocation == null)
                    cacheLocation = 
                        System.getProperty("user.home") + File.separator
                        + "Library" + File.separator
                        + "Caches";
                if (logLocation == null)
                    logLocation = 
                        System.getProperty("user.home") + File.separator
                        + "Library" + File.separator
                        + "Logs";

                if (name == null)
                    name = "Jitsi";
            }
            else if (osName.startsWith("Windows"))
            {
                /*
                 * Primarily important on Vista because Windows Explorer opens
                 * in %USERPROFILE% so .sip-communicator is always visible. But
                 * it may be a good idea to follow the OS recommendations and
                 * use APPDATA on pre-Vista systems as well.
                 */
                if (profileLocation == null)
                    profileLocation = System.getenv("APPDATA");
                if (cacheLocation == null)
                    cacheLocation = System.getenv("LOCALAPPDATA");
                if (logLocation == null)
                    logLocation = System.getenv("LOCALAPPDATA");
                if (name == null)
                    name = "Jitsi";
            }

            /* If there're no OS specifics, use the defaults. */
            if (profileLocation == null)
                profileLocation = defaultLocation;
            if (cacheLocation == null)
                cacheLocation = profileLocation;
            if (logLocation == null)
                logLocation = profileLocation;
            if (name == null)
                name = defaultName;

            /*
             * As it was noted earlier, make sure we're compatible with previous
             * releases. If the home dir name is forced (set as system property)
             * doesn't look for the default dir.
             */
            if (!isHomeDirnameForced
                && (new File(profileLocation, name).isDirectory() == false)
                && new File(defaultLocation, defaultName).isDirectory())
            {
                profileLocation = defaultLocation;
                name = defaultName;
            }

            // if we need to check legacy names and there is no current home dir
            // already created
            if(chekLegacyDirNames
                    && !checkHomeFolderExist(profileLocation, name, osName))
            {
                // now check whether a legacy dir name exists and use it
                for(int i = 0; i < LEGACY_DIR_NAMES.length; i++)
                {
                    String dir = LEGACY_DIR_NAMES[i];

                    // check the platform specific directory
                    if(checkHomeFolderExist(profileLocation, dir, osName))
                    {
                        name = dir;
                        break;
                    }

                    // now check it and in the default location
                    if(checkHomeFolderExist(defaultLocation, dir, osName))
                    {
                        name = dir;
                        profileLocation = defaultLocation;
                        break;
                    }
                }
            }

            System.setProperty(PNAME_SC_HOME_DIR_LOCATION, profileLocation);
            System.setProperty(PNAME_SC_CACHE_DIR_LOCATION, cacheLocation);
            System.setProperty(PNAME_SC_LOG_DIR_LOCATION, logLocation);
            System.setProperty(PNAME_SC_HOME_DIR_NAME, name);
        }

        // when we end up with the home dirs, make sure we have log dir
        new File(new File(logLocation, name), "log").mkdirs();
    }

    /**
     * Checks whether home folder exists. Special situation checked under
     * macosx, due to created folder of the new version of the updater we may
     * end up with our settings in 'SIP Communicator' folder and having 'Jitsi'
     * folder created by the updater(its download location). So we check not
     * only the folder exist but whether it contains any of the known
     * configuration files in it.
     *
     * @param parent the parent folder
     * @param name the folder name to check.
     * @param osName OS name
     * @return whether folder exists.
     */
    static boolean checkHomeFolderExist(
            String parent, String name, String osName)
    {
        if(osName.startsWith("Mac"))
        {
            for(int i = 0; i < LEGACY_CONFIGURATION_FILE_NAMES.length; i++)
            {
                String f = LEGACY_CONFIGURATION_FILE_NAMES[i];

                if(new File(new File(parent, name), f).exists())
                    return true;
            }
            return false;
        }

        return new File(parent, name).isDirectory();
    }

    /**
     * Sets some system properties specific to the OS that needs to be set at
     * the very beginning of a program (typically for UI related properties,
     * before AWT is launched).
     *
     * @param osName OS name
     */
    private static void setSystemProperties(String osName)
    {
        // setup here all system properties that need to be initialized at
        // the very beginning of an application
        if(osName.startsWith("Windows"))
        {
            // disable Direct 3D pipeline (used for fullscreen) before
            // displaying anything (frame, ...)
            System.setProperty("sun.java2d.d3d", "false");
        }
        else if(osName.startsWith("Mac"))
        {
            // On Mac OS X when switch in fullscreen, all the monitors goes
            // fullscreen (turns black) and only one monitors has images
            // displayed. So disable this behavior because somebody may want
            // to use one monitor to do other stuff while having other ones with
            // fullscreen stuff.
            System.setProperty("apple.awt.fullscreencapturealldisplays",
                "false");
        }
    }
    
    
    
    
    
    public static void main(String[] args)
        throws InterruptedException
    {
        String version = System.getProperty("java.version");
        String vmVendor = System.getProperty("java.vendor");
        String osName = System.getProperty("os.name");

        setSystemProperties(osName);

        /*
         * SC_HOME_DIR_* are specific to the OS so make sure they're configured
         * accordingly before any other application-specific logic depending on
         * them starts (e.g. Felix).
         */
        setScHomeDir(osName);

        // this needs to be set before any DNS lookup is run
        File f
            = new File(
                    System.getProperty(PNAME_SC_HOME_DIR_LOCATION),
                    System.getProperty(PNAME_SC_HOME_DIR_NAME)
                        + File.separator
                        + ".usednsjava");
        if(f.exists())
        {
            System.setProperty(
                    "sun.net.spi.nameservice.provider.1",
                    "dns,dnsjava");
        }

        if (version.startsWith("1.4") || vmVendor.startsWith("Gnu") ||
                vmVendor.startsWith("Free"))
        {
            String os = "";

            if (osName.startsWith("Mac"))
                os = ChangeJVMFrame.MAC_OSX;
            else if (osName.startsWith("Linux"))
                os = ChangeJVMFrame.LINUX;
            else if (osName.startsWith("Windows"))
                os = ChangeJVMFrame.WINDOWS;

            ChangeJVMFrame changeJVMFrame = new ChangeJVMFrame(os);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            changeJVMFrame.setLocation(
                screenSize.width/2 - changeJVMFrame.getWidth()/2,
                screenSize.height/2 - changeJVMFrame.getHeight()/2);
            changeJVMFrame.setVisible(true);

            return;
        }
        
        
        
        
        
        java.util.logging.Logger l = java.util.logging.Logger.getLogger("");
        l.setLevel(java.util.logging.Level.WARNING);
        
        HostInfo infoCLI = new HostInfo();
        CmdLineParser parser = new CmdLineParser(infoCLI);
        try {
            parser.parseArgument(args);
        }
        catch(CmdLineException e)
        {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return;
        }
        
        //This HostInfo is used for tests in my localhost XMPP server
        /*
        infoCLI = new HostInfo(
                "jitmeet.example.com",
                "conference.jitmeet.example.com",
                5222,
                "jitsi-videobridge.lambada.jitsi.net",
                "HammerTest");
        */
        
        //We create a Hammer with only 1 user for now
        Hammer hammer = new Hammer(infoCLI,"JitMeet-Hammer",1);
        
        //We call initialize the Hammer (registering OSGi bundle for example)
        hammer.init();
        //After the initialization we start the Hammer (all its users will
        //connect to the XMPP server and try to setup media stream with it bridge
        hammer.start();
        while(true) Thread.sleep(3600000);
    }
}
