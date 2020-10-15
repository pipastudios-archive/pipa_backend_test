package tester.hr.interfaces;

/**
 * A user score containing the user id, its score and ranking position
 */
public interface IScore
{
    default String ticker()
    {
        return "Score[" + getPosition() + "," + getUserId() + "," + getScore() + "]";
    }
    
    /** The user id */
    String getUserId();
    
    /** The user score */
    long getScore();
    
    /** User position in the ranking, where 1 is the first position and n the n-th position */
    int getPosition();
}
