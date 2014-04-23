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

public class Question implements Comparable<Question>
{
    private final String question;
    private final String answer;
    private final String[] keywords;
    private int hits;

    public Question(String question, String answer, String[] keywords)
    {
        this.question = question;
        this.answer = answer;
        this.keywords = keywords;
        this.hits = 0;
    }

    public String getQuestion()
    {
        return question;
    }

    public String getAnswer()
    {
        return answer;
    }

    public String[] getKeywords()
    {
        return keywords;
    }
    public double getScore(String message)
    {
        if (question.equalsIgnoreCase(message))
        {
            return -1;
        }
        double score = 0;
        int lastOffset = 0;
        int p = 0;
        for (String keyword : keywords)
        {
            for (;(lastOffset = message.indexOf(keyword, lastOffset + p)) > -1; score++)
            {
                p = 1;
            }
        }
        score *= (score / (double) keywords.length);
        return score;
    }

    public void hit()
    {
        this.hits++;
    }

    public int compareTo(Question o)
    {
        if (this.hits > o.hits)
        {
            return 1;
        }
        else if (this.hits < o.hits)
        {
            return -1;
        }
        return 0;
    }
}
