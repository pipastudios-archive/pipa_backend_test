package tester.hr.interfaces;

import java.util.List;

/**
 * This is a score service that accepts points from users and return an up-to-date sorted list of user scores 
 */
public interface IScoreService
{
    /** 
     * Post a user point score
     * @userId the user id
     * @points the amount of points scored by the user, to be added to its score
     */
    void postScore(String userId, long points);
    
    /**
     * Retrieve a user score
     * @userId the user id
     * @returns the score object associated with this user or null if the user hasn't scored any points yet
     */
    <S extends IScore> S retrieveScore(String userId);
    
    /**
     * Retrieves the ranking
     * @returns the sorted list of scores from higher to lower score
     */
    <S extends IScore> List<S> retrieveRanking();
}
