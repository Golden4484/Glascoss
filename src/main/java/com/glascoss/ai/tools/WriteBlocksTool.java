package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import com.glascoss.ai.CommandFilter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;

public class WriteBlocksTool implements Tool {
    // 5x7 bitmap font: each char is 5 bytes (one per column), each byte = 7 rows
    private static final Map<Character, int[]> FONT = new HashMap<>();

    static {
        FONT.put('A', new int[]{0b01110, 0b10001, 0b10001, 0b11111, 0b10001});
        FONT.put('B', new int[]{0b11110, 0b10001, 0b11110, 0b10001, 0b11110});
        FONT.put('C', new int[]{0b01110, 0b10001, 0b10000, 0b10001, 0b01110});
        FONT.put('D', new int[]{0b11110, 0b10001, 0b10001, 0b10001, 0b11110});
        FONT.put('E', new int[]{0b11111, 0b10000, 0b11110, 0b10000, 0b11111});
        FONT.put('F', new int[]{0b11111, 0b10000, 0b11110, 0b10000, 0b10000});
        FONT.put('G', new int[]{0b01110, 0b10000, 0b10111, 0b10001, 0b01110});
        FONT.put('H', new int[]{0b10001, 0b10001, 0b11111, 0b10001, 0b10001});
        FONT.put('I', new int[]{0b01110, 0b00100, 0b00100, 0b00100, 0b01110});
        FONT.put('J', new int[]{0b00111, 0b00010, 0b00010, 0b10010, 0b01100});
        FONT.put('K', new int[]{0b10001, 0b10010, 0b11100, 0b10010, 0b10001});
        FONT.put('L', new int[]{0b10000, 0b10000, 0b10000, 0b10000, 0b11111});
        FONT.put('M', new int[]{0b10001, 0b11011, 0b10101, 0b10001, 0b10001});
        FONT.put('N', new int[]{0b10001, 0b11001, 0b10101, 0b10011, 0b10001});
        FONT.put('O', new int[]{0b01110, 0b10001, 0b10001, 0b10001, 0b01110});
        FONT.put('P', new int[]{0b11110, 0b10001, 0b11110, 0b10000, 0b10000});
        FONT.put('Q', new int[]{0b01110, 0b10001, 0b10101, 0b10010, 0b01101});
        FONT.put('R', new int[]{0b11110, 0b10001, 0b11110, 0b10010, 0b10001});
        FONT.put('S', new int[]{0b01111, 0b10000, 0b01110, 0b00001, 0b11110});
        FONT.put('T', new int[]{0b11111, 0b00100, 0b00100, 0b00100, 0b00100});
        FONT.put('U', new int[]{0b10001, 0b10001, 0b10001, 0b10001, 0b01110});
        FONT.put('V', new int[]{0b10001, 0b10001, 0b10001, 0b01010, 0b00100});
        FONT.put('W', new int[]{0b10001, 0b10001, 0b10101, 0b11011, 0b10001});
        FONT.put('X', new int[]{0b10001, 0b01010, 0b00100, 0b01010, 0b10001});
        FONT.put('Y', new int[]{0b10001, 0b01010, 0b00100, 0b00100, 0b00100});
        FONT.put('Z', new int[]{0b11111, 0b00010, 0b00100, 0b01000, 0b11111});
        FONT.put('0', new int[]{0b01110, 0b10011, 0b10101, 0b11001, 0b01110});
        FONT.put('1', new int[]{0b00100, 0b01100, 0b00100, 0b00100, 0b01110});
        FONT.put('2', new int[]{0b01110, 0b00001, 0b00110, 0b01000, 0b11111});
        FONT.put('3', new int[]{0b11110, 0b00001, 0b00110, 0b00001, 0b11110});
        FONT.put('4', new int[]{0b00010, 0b00110, 0b01010, 0b11111, 0b00010});
        FONT.put('5', new int[]{0b11111, 0b10000, 0b11110, 0b00001, 0b11110});
        FONT.put('6', new int[]{0b00110, 0b01000, 0b11110, 0b10001, 0b01110});
        FONT.put('7', new int[]{0b11111, 0b00001, 0b00010, 0b00100, 0b00100});
        FONT.put('8', new int[]{0b01110, 0b10001, 0b01110, 0b10001, 0b01110});
        FONT.put('9', new int[]{0b01110, 0b10001, 0b01111, 0b00010, 0b01100});
        FONT.put('!', new int[]{0b00100, 0b00100, 0b00100, 0b00000, 0b00100});
        FONT.put('?', new int[]{0b01110, 0b10001, 0b00110, 0b00000, 0b00100});
        FONT.put('.', new int[]{0b00000, 0b00000, 0b00000, 0b00000, 0b00100});
        FONT.put(',', new int[]{0b00000, 0b00000, 0b00000, 0b00100, 0b01000});
        FONT.put('\'', new int[]{0b00100, 0b00100, 0b00000, 0b00000, 0b00000});
        FONT.put(':', new int[]{0b00000, 0b00100, 0b00000, 0b00100, 0b00000});
        FONT.put(';', new int[]{0b00000, 0b00100, 0b00000, 0b00100, 0b01000});
        FONT.put('-', new int[]{0b00000, 0b00000, 0b11111, 0b00000, 0b00000});
        FONT.put('+', new int[]{0b00000, 0b00100, 0b01110, 0b00100, 0b00000});
        FONT.put('=', new int[]{0b00000, 0b11111, 0b00000, 0b11111, 0b00000});
        FONT.put('/', new int[]{0b00001, 0b00010, 0b00100, 0b01000, 0b10000});
        FONT.put(' ', new int[]{0b00000, 0b00000, 0b00000, 0b00000, 0b00000});
    }

    @Override
    public String getName() { return "write_blocks"; }

    @Override
    public String getDescription() { return "Write text using blocks. Parameters: text (string, max 30 chars), x (int), y (int), z (int), block (string, default minecraft:stone), direction (east/west/north/south, default east)"; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            String text = ArgParser.getString(args, "text", "").toUpperCase();
            int x = ArgParser.resolveCoord(ArgParser.getString(args, "x", "~"), (int) player.getX());
            int y = ArgParser.resolveCoord(ArgParser.getString(args, "y", "~"), (int) player.getY());
            int z = ArgParser.resolveCoord(ArgParser.getString(args, "z", "~"), (int) player.getZ());
            String block = ArgParser.getString(args, "block", "minecraft:stone");
            String direction = ArgParser.getString(args, "direction", "east");

            if (text.isBlank()) return "No text provided.";
            if (text.length() > 30) return "Text too long (max 30 chars).";

            if (!block.contains(":")) block = "minecraft:" + block;

            int dx = 0, dz = 0;
            switch (direction) {
                case "east" -> dx = 1;
                case "west" -> dx = -1;
                case "south" -> dz = 1;
                case "north" -> dz = -1;
                default -> dx = 1;
            }

            int charWidth = 6; // 5 pixels + 1 space
            int placed = 0;

            for (int ci = 0; ci < text.length(); ci++) {
                char c = text.charAt(ci);
                int[] bitmap = FONT.getOrDefault(c, new int[]{0, 0, 0, 0, 0});

                for (int col = 0; col < 5; col++) {
                    int mask = bitmap[col];
                    for (int row = 0; row < 7; row++) {
                        if ((mask & (1 << (6 - row))) != 0) {
                            int bx = x + (ci * charWidth + col) * dx;
                            int by = y + (6 - row);
                            int bz = z + (ci * charWidth + col) * dz;

                            String setblock = String.format("setblock %d %d %d %s",
                                    bx, by, bz, block);
                            int result = CommandFilter.executeSilently(setblock, world, player);
                            if (result > 0) placed++;
                        }
                    }
                }
            }

            return "Wrote \"" + text + "\" using " + placed + " " + block + " blocks at [" + x + ", " + y + ", " + z + "] facing " + direction + ".";
        } catch (Exception e) {
            return "write_blocks error: " + e.getMessage();
        }
    }
}
