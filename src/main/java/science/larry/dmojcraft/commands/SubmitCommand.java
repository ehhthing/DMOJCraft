package science.larry.dmojcraft.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jsoup.HttpStatusException;
import science.larry.dmojcraft.DMOJCraft;
import science.larry.dmojcraft.dmoj.Language;
import science.larry.dmojcraft.dmoj.SubmissionResult;
import science.larry.dmojcraft.dmoj.Testcase;
import science.larry.dmojcraft.dmoj.UserSession;
import science.larry.dmojcraft.exceptions.InvalidSessionException;
import science.larry.dmojcraft.util.RateLimiter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SubmitCommand implements CommandExecutor {
    RateLimiter limiter = new RateLimiter(1, 10000);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            return false;
        }

        Player player = ((Player) sender);

        if (!DMOJCraft.authCache.has(player)) {
            sender.sendMessage(ChatColor.RED + "Please authenticate using /auth before submitting");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!item.getType().equals(Material.WRITABLE_BOOK) && !item.getType().equals(Material.WRITTEN_BOOK)) {
            sender.sendMessage(ChatColor.RED + "Please hold the book of code you would like to submit.");
            return true;
        }

        BookMeta bookData = (BookMeta) item.getItemMeta();
        if (bookData == null) {
            sender.sendMessage(ChatColor.RED + "Invalid book data received.");
            return true;
        }

        if (bookData.hasGeneration()) {
            sender.sendMessage(ChatColor.RED + "Copying code is not allowed!");
            return true;
        }

        String code = ChatColor.stripColor(String.join("", bookData.getPages()).trim());
        if (code.length() == 0) {
            sender.sendMessage(ChatColor.RED + "Please write some code!");
            return true;
        }

        Integer languageId = Language.getId(args[1]);
        if (languageId == null) {
            sender.sendMessage(ChatColor.RED + "Invalid language!\nValid languages are: " + ChatColor.YELLOW + String.join(", ", Language.getLanguages()));
            return true;
        }

        if (limiter.invoke(player)) {
            sender.sendMessage(ChatColor.RED + "Please wait before submitting again! (1 time every 10 seconds)");
            return true;
        }

        UserSession session = DMOJCraft.authCache.get(player);
        int submissionID;
        try {
            submissionID = session.submit(args[0], languageId, code);
        } catch (InvalidSessionException e) {
            DMOJCraft.authCache.unauthenticate(player);
            return true;
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 404) {
                sender.sendMessage(ChatColor.RED + "Invalid problem code.");
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to submit: " + e.getMessage());
                e.printStackTrace();
            }
            return true;
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Something died, like really badly. Join the Slack and yell at Larry for being bad.");
            e.printStackTrace();
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "Queued submission " + ChatColor.YELLOW + submissionID);

        new Thread(() -> {
            DMOJCraft.logger.info("Starting thread to poll for changes on submission " + submissionID);
            boolean first = true;
            int counter = 0;
            SubmissionResult result = null;
            Set<Integer> seenCases = new HashSet<>();
            do {
                try {
                    Thread.sleep(2000);
                    result = session.getTestcaseStatus(submissionID);
                } catch (InvalidSessionException e) {
                    DMOJCraft.authCache.unauthenticate(player);
                    break;
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + "We had a problem while fetching submission results " + e.getMessage());
                    e.printStackTrace();
                    break;
                } catch (InterruptedException ignored) {
                    break;
                }

                if (first) {
                    sender.sendMessage(ChatColor.GREEN + "Submission to " + ChatColor.YELLOW + result.problemName + ChatColor.GREEN + " by " + ChatColor.YELLOW + session.getUser());
                    first = false;
                }

                for (Testcase testcase : result.cases) {
                    if (seenCases.contains(testcase.id)) continue;
                    sender.sendMessage(testcase.descriptor + " " + testcase.status + " " + testcase.details);
                    seenCases.add(testcase.id);
                }

                if (++counter >= 30) {
                    sender.sendMessage(ChatColor.RED + "Your submission took too long, so we gave up on polling for new changes.");
                    break;
                }
            } while (!result.done);
            if (result != null && result.done) {
                if (result.cases.size() > 0) {
                    sender.sendMessage(ChatColor.GREEN + "Resources\n"
                            + "Time: " + ChatColor.YELLOW + result.time
                            + ChatColor.GREEN + " Memory: " + ChatColor.YELLOW + result.memory
                    );
                } else {
                    sender.sendMessage(ChatColor.YELLOW + result.raw_result);
                }
            }
            DMOJCraft.logger.info("Finished polling for " + submissionID);
        }).start();

        return true;
    }
}
