package nz.co.jammehcow.lukkit.environment.plugin;

import nz.co.jammehcow.lukkit.LukkitContainer;
import nz.co.jammehcow.lukkit.Utilities;
import nz.co.jammehcow.lukkit.environment.LuaEnvironment;
import nz.co.jammehcow.lukkit.environment.plugin.commands.LukkitCommand;
import nz.co.jammehcow.lukkit.environment.wrappers.ConfigWrapper;
import nz.co.jammehcow.lukkit.environment.wrappers.LoggerWrapper;
import nz.co.jammehcow.lukkit.environment.wrappers.PluginWrapper;
import nz.co.jammehcow.lukkit.environment.wrappers.UtilitiesWrapper;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class InternalLuaPlugin {
    private final String name;
    private final String version;
    private final LukkitPluginFile pluginFile;
    private final LuaValue pluginMain;
    private final List<LukkitCommand> commands = new ArrayList<>();
    private final HashMap<Class<? extends Event>, ArrayList<LuaFunction>> eventListeners = new HashMap<>();
    private UtilitiesWrapper utilitiesWrapper;
    private LuaFunction loadCB, enableCB, disableCB;
    private boolean enabled = false;
    private Logger logger;
    private File dataFolder;

    public InternalLuaPlugin(LukkitPluginFile file) throws Exception {
        this.pluginFile = file;
        PluginDescriptionFile descriptor = new PluginDescriptionFile(this.pluginFile.getPluginYML());
        this.name = descriptor.getName();
        this.version = descriptor.getVersion();
        this.logger = Logger.getLogger("Lukkit-" + name);
        this.dataFolder = new File(LukkitContainer.getInstance().getDataFolder().getParentFile(), name);
        if (!this.dataFolder.exists()) this.dataFolder.mkdir();
        
        Globals globals = LuaEnvironment.getNewGlobals(this);
        this.pluginMain = globals.load(new java.io.InputStreamReader(
            this.pluginFile.getResource(descriptor.getMain()), 
            java.nio.charset.StandardCharsets.UTF_8), 
            descriptor.getMain()
        );
        
        setupPluginGlobals(globals);
        this.pluginMain.call();
        if (this.loadCB != null) this.loadCB.call();
    }

    public void enable() {
        this.enabled = true;
        if (this.enableCB != null) this.enableCB.call(CoerceJavaToLua.coerce(this));
        eventListeners.forEach((event, list) -> list.forEach(function ->
            LukkitContainer.getInstance().getServer().getPluginManager().registerEvent(
                event, new Listener() {}, EventPriority.NORMAL, 
                (l, e) -> function.call(CoerceJavaToLua.coerce(e)), 
                LukkitContainer.getInstance(), false
            )
        ));
        logger.info("Enabled " + name + " v" + version);
    }

    public void disable() {
        this.enabled = false;
        if (this.disableCB != null) this.disableCB.call(CoerceJavaToLua.coerce(this));
        unregisterAllCommands();
        if (utilitiesWrapper != null) utilitiesWrapper.close();
        logger.info("Disabled " + name);
    }

    public String getName() { return name; }
    public String getVersion() { return version; }
    public boolean isEnabled() { return enabled; }
    public Logger getLogger() { return logger; }
    public File getDataFolder() { return dataFolder; }
    public InputStream getResource(String path) { return pluginFile.getResource(path); }
    public LukkitPluginFile getPluginFile() { return pluginFile; }
    public void setLoadCB(LuaFunction cb) { this.loadCB = cb; }
    public void setEnableCB(LuaFunction cb) { this.enableCB = cb; }
    public void setDisableCB(LuaFunction cb) { this.disableCB = cb; }

    public void registerCommand(LukkitCommand command) {
        commands.add(command);
        try { command.register(); } 
        catch (Exception e) { logger.severe("Failed to register command: " + e.getMessage()); }
    }

    public void unregisterCommand(LukkitCommand command) {
        commands.remove(command);
        try { command.unregister(); } 
        catch (Exception e) { logger.severe("Failed to unregister command: " + e.getMessage()); }
    }

    public void unregisterAllCommands() {
        new ArrayList<>(commands).forEach(this::unregisterCommand);
    }

    public Listener registerEvent(Class<? extends Event> event, LuaFunction function) {
        getEventListeners(event).add(function);
        if (this.enabled) {
            LukkitContainer.getInstance().getServer().getPluginManager().registerEvent(
                event, new Listener() {}, EventPriority.NORMAL, 
                (l, e) -> function.call(CoerceJavaToLua.coerce(e)), 
                LukkitContainer.getInstance(), false
            );
        }
        return null;
    }

    private ArrayList<LuaFunction> getEventListeners(Class<? extends Event> event) {
        this.eventListeners.computeIfAbsent(event, k -> new ArrayList<>());
        return this.eventListeners.get(event);
    }

    private void setupPluginGlobals(Globals globals) {
        globals.set("plugin", new PluginWrapper(this));
        globals.set("logger", new LoggerWrapper(this));
        utilitiesWrapper = new UtilitiesWrapper(this);
        globals.set("util", utilitiesWrapper);
        globals.set("config", new ConfigWrapper(this));

        OneArgFunction oldRequire = (OneArgFunction) globals.get("require");
        globals.set("require", new OneArgFunction() {
            public LuaValue call(LuaValue luaValue) {
                String path = luaValue.checkjstring();
                if (!path.endsWith(".lua")) path += ".lua";
                path = path.replaceAll("\\.(?=[^.]*\\.)", "/");
                InputStream resource = pluginFile.getResource(path);
                if (resource == null) return oldRequire.call(luaValue);
                try {
                    return globals.load(new java.io.InputStreamReader(resource, "UTF-8"), path.replace("/", ".")).call();
                } catch (UnsupportedEncodingException e) { e.printStackTrace(); }
                return NIL;
            }
        });

        globals.set("import", new OneArgFunction() {
            public LuaValue call(LuaValue luaValue) {
                try {
                    String path = luaValue.checkjstring();
                    if (path.startsWith("$")) path = "org.bukkit" + path.substring(1);
                    if (path.startsWith("#")) path = "nz.co.jammehcow.lukkit.environment" + path.substring(1);
                    return CoerceJavaToLua.coerce(Class.forName(path));
                } catch (ClassNotFoundException e) { e.printStackTrace(); }
                return NIL;
            }
        });

        globals.set("newInstance", new VarArgFunction() {
            public LuaValue invoke(Varargs vargs) {
                String classPath = vargs.checkjstring(1);
                LuaValue args = vargs.optvalue(2, LuaValue.NIL);
                if (classPath.startsWith("$")) classPath = "org.bukkit" + classPath.substring(1);
                else if (classPath.startsWith("#")) classPath = "nz.co.jammehcow.lukkit.environment" + classPath.substring(1);
                if (!Utilities.isClassPathValid(classPath)) {
                    LukkitPluginException exception = new LukkitPluginException("Invalid classpath: " + classPath);
                    LuaEnvironment.addError(exception); throw exception;
                }
                LuaString classPathValue = LuaValue.valueOf(classPath);
                LuaValue newInstanceMethod = globals.get("luajava").get("newInstance");
                switch (args.type()) {
                    case LuaValue.TNIL: return newInstanceMethod.invoke(classPathValue).checkvalue(1);
                    case LuaValue.TTABLE:
                        LuaTable argTable = args.checktable();
                        LuaValue[] varargArray = new LuaValue[argTable.length() + 1];
                        varargArray[0] = classPathValue;
                        for (int iKey = 1; iKey < varargArray.length; iKey++) {
                            varargArray[iKey] = argTable.get(iKey);
                        }
                        return newInstanceMethod.invoke(varargArray).checkvalue(1);
                    default:
                        LukkitPluginException exception = new LukkitPluginException("Second argument must be table");
                        LuaEnvironment.addError(exception); throw exception;
                }
            }
        });
    }
}
