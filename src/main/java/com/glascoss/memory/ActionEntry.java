package com.glascoss.memory;

public class ActionEntry {
    private final String toolName;
    private final String args;
    private final String result;
    private final long timestamp;
    private final String playerName;
    private final boolean sabotaged;

    public ActionEntry(String toolName, String args, String result, String playerName, boolean sabotaged) {
        this.toolName = toolName;
        this.args = args;
        this.result = result;
        this.timestamp = System.currentTimeMillis();
        this.playerName = playerName;
        this.sabotaged = sabotaged;
    }

    public ActionEntry(String toolName, String args, String result) {
        this(toolName, args, result, "system", false);
    }

    public String getToolName() { return toolName; }
    public String getArgs() { return args; }
    public String getResult() { return result; }
    public long getTimestamp() { return timestamp; }
    public String getPlayerName() { return playerName; }
    public boolean isSabotaged() { return sabotaged; }
}
