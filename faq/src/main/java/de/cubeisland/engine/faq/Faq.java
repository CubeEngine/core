/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.faq;

import java.util.PriorityQueue;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.cubeisland.engine.core.module.Module;

public class Faq extends Module implements Listener
{
    private final PriorityQueue<Question> questions = new PriorityQueue<>();
    
    @Override
    public void onEnable()
    {
        getCore().getConfigFactory().getDefaultConverterManager().registerConverter(Question.class, new QuestionConverter());

        FaqConfig config = this.loadConfig(FaqConfig.class);
        this.questions.addAll(config.questions);
        this.getCore().getEventManager().registerListener(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        String question = event.getMessage();
        int questionMarkIndex = question.indexOf('?');
        if (questionMarkIndex > -1)
        {
            question = question.substring(0, questionMarkIndex + 1);
            int dotPosition = question.indexOf('.');
            if (dotPosition > -1 && dotPosition < questionMarkIndex - 2)
            {
                question = question.substring(dotPosition + 1);
            }
            question = question.toLowerCase();

            double score;
            double highestScore = 0;
            Question bestFaq = null;

            for (Question faq : this.questions)
            {
                score = faq.getScore(question);
                if (score == -1 || score > highestScore)
                {
                    highestScore = score;
                    bestFaq = faq;
                    if (highestScore == -1)
                    {
                        break;
                    }
                }
            }

            if (bestFaq != null && (highestScore >= 1.0 || highestScore == -1))
            {
                bestFaq.hit();
                final String answer = bestFaq.getAnswer();
                final Player player = event.getPlayer();
                this.getCore().getTaskManager().runTaskDelayed(this, new Runnable() {
                    public void run()
                    {
                        player.sendMessage(answer);
                    }
                }, 5L);
            }
        }
    }
}
