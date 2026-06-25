package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocateAndTeleportTool implements Tool {
    private static final Pattern COORDS_BRACKET = Pattern.compile("\\[\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\]");
    private static final Pattern COORDS_AT = Pattern.compile("at\\s+(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)", Pattern.CASE_INSENSITIVE);
    private static final String[] VILLAGE_VARIANTS = {
        "minecraft:village_plains", "minecraft:village_desert", "minecraft:village_savanna",
        "minecraft:village_taiga", "minecraft:village_snowy"
    };

    @Override
    public String getName() { return "tp_to_structure"; }

    @Override
    public String getDescription() { return "Locate a structure and teleport the player to it. Parameters: name (string, e.g. village, fortress, mansion)"; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            String structureName = ArgParser.getString(args, "name", "village").toLowerCase().trim();

            // Normalize: if no namespace, try minecraft: prefix
            String locateName = structureName.contains(":") ? structureName : "minecraft:" + structureName;

            String locateResult = tryLocate(locateName, world, player);

            // If village and not found, try all biome variants
            if (isNotFound(locateResult) && structureName.contains("village")) {
                for (String variant : VILLAGE_VARIANTS) {
                    locateResult = tryLocate(variant, world, player);
                    if (!isNotFound(locateResult)) {
                        locateName = variant;
                        break;
                    }
                }
            }

            // Fallback: try without "structure" subcommand
            if (isNotFound(locateResult)) {
                locateResult = tryLocateLegacy(locateName, world, player);
            }

            // Final fallback: try just the name as-is
            if (isNotFound(locateResult) && !structureName.contains(":")) {
                locateResult = tryLocate(structureName, world, player);
                if (!isNotFound(locateResult)) locateName = structureName;
            }

            if (isNotFound(locateResult)) {
                return "Could not find structure: " + structureName + " in this world.";
            }

            Matcher m = COORDS_BRACKET.matcher(locateResult);
            if (!m.find()) {
                m = COORDS_AT.matcher(locateResult);
            }
            if (!m.find()) {
                String raw = runSilentCommand("locate structure " + locateName, world, player);
                if (raw == null || raw.isBlank())
                    raw = runSilentCommand("locate " + locateName, world, player);
                return "Found " + locateName + ", but could not parse coordinates. Raw: " + (raw != null ? raw : "empty");
            }

            int x = Integer.parseInt(m.group(1));
            int y = Integer.parseInt(m.group(2));
            int z = Integer.parseInt(m.group(3));

            int safeY = findSafeY(world, x, z, y);

            String tpCmd = String.format("tp @p %d %d %d", x, safeY, z);
            String tpResult = runSilentCommand(tpCmd, world, player);

            if (tpResult != null && tpResult.toLowerCase().contains("error")) {
                return "Teleport failed: " + tpResult;
            }

            return "Found " + locateName + " at [" + x + ", " + safeY + ", " + z + "]. Teleported you there.";
        } catch (Exception e) {
            return "tp_to_structure error: " + e.getMessage();
        }
    }

    private boolean isNotFound(String result) {
        if (result == null || result.isBlank()) return true;
        String lower = result.toLowerCase();
        return lower.contains("not found") || lower.contains("não encontrada")
            || lower.contains("nenhuma estrutura") || lower.contains("could not locate")
            || lower.contains("no structure") || lower.contains("não encontrado")
            || lower.contains("argumento incorreto") || lower.contains("incorrect argument")
            || lower.contains("unknown") || lower.contains("desconhecido")
            || lower.contains("invalid") || lower.contains("inválido")
            || lower.contains("não existe") || lower.contains("does not exist");
    }

    private String tryLocate(String fullName, ServerWorld world, PlayerEntity player) {
        String result = runSilentCommand("locate structure " + fullName, world, player);
        if (result != null && !result.isBlank()) return result;
        return runSilentCommand("locate " + fullName, world, player);
    }

    private String tryLocateLegacy(String fullName, ServerWorld world, PlayerEntity player) {
        return runSilentCommand("locate " + fullName, world, player);
    }

    private String runSilentCommand(String command, ServerWorld world, PlayerEntity player) {
        MinecraftServer server = world.getServer();
        StringBuilder output = new StringBuilder();

        CommandOutput capture = new CommandOutput() {
            @Override public void sendMessage(Text message) {
                if (output.length() > 0) output.append("\n");
                output.append(message.getString());
            }
            @Override public boolean shouldReceiveFeedback() { return true; }
            @Override public boolean shouldTrackOutput() { return true; }
            @Override public boolean shouldBroadcastConsoleToOps() { return false; }
        };

        ServerCommandSource source = new ServerCommandSource(
                capture,
                player.getPos(),
                Vec2f.ZERO,
                world,
                2,
                player.getName().getString(),
                player.getDisplayName(),
                server,
                null
        );

        server.getCommandManager().executeWithPrefix(source, "/" + command);
        return output.toString();
    }

    private int findSafeY(ServerWorld world, int x, int z, int hintY) {
        for (int dy = 0; dy < 20; dy++) {
            int checkY = hintY + dy;
            if (isSafeAt(world, x, checkY, z)) return checkY;
            checkY = hintY - dy;
            if (isSafeAt(world, x, checkY, z)) return checkY;
        }
        return hintY;
    }

    private boolean isSafeAt(ServerWorld world, int x, int y, int z) {
        if (y < world.getBottomY() + 1 || y > world.getTopY()) return false;
        return world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)).isAir()
                && world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z)).isAir()
                && !world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z)).isAir();
    }
}
