package tester.hr.validation;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import tester.hr.interfaces.IScoreService;

abstract public class Validator 
{
    static private IScoreService resolveImplementation()
    {
        // TODO return your ScoreService implementation
        return new tester.hr.implementations.benchmark.ScoreService();
    }
    
    static private class ScoreOperation
    {
        public final String userId;
        public final long points;
        
        public ScoreOperation(String userId, long points)
        {
            this.userId = userId;
            this.points = points;
        }
        
        @Override
        public String toString()
        {
            return "[" + userId + "," + points + "]";
        }
    }
    
    static private final String[] NAMES = new String[]
    {
        "Orval",
        "Georgiana",
        "Paul",
        "Merlene",
        "Nicola",
        "Landon",
        "Nobuko",
        "Tory",
        "Leeanne",
        "Elwanda",
        "Lashonda",
        "William",
        "Belle",
        "Rosaura",
        "Isaura",
        "Jesenia",
        "Wes",
        "Shawanda",
        "Jill",
        "Levi",
        "Dennise",
        "Yolando",
        "Jacinda",
        "Masako",
        "Keli",
        "Madalene",
        "Mohammed",
        "Dalia",
        "Hung",
        "Carola",
        "Graig",
        "Emmaline",
        "Jimmy",
        "Phylicia",
        "Nathanael",
        "Joanie",
        "Shala",
        "Christie",
        "Alverta",
        "Titus",
        "Maryetta",
        "Melia",
        "Isabell",
        "Rosie",
        "Earline",
        "Kristopher",
        "Sherwood",
        "Alvin",
        "Dominga",
        "Emiko"
    };
        
    static private final String BIG_HASH = 
        "Rd63atXh2wtylgQkojCxWnuUOOtv/eLrebkLNCKO0tiwHqksevjBtkj4vp5tVF+XZbqSx6JghVoY6uJmJM/nP4H4AVToOfJkr5UpJtbAsblh6l/ptiLdMs3fznbn6W0X76U+4ewIekIA/GsmMd6NOcQXPGbGlmpGHzB/Gi6gwq5vUv804esgJlA9Bievee1BpE8A3jIz1HTKz7PY6mP8tyiz6SK9nCZSAMx6EJhM1QopRb/OOkjTCDPUfVuAuywb7BS56q1aaZ6GvPVVoF5FjEHqu3qNaT+2jg7wpPxPDF5AEnGGHjn9arnQ/nNmD9H1N9iajri9f/mdCdzB3Zk3" +
        "0MZgc+/JiIrIbfK4cl1qYFpMeQFl0BcUMJ/kY/KJ7k1P6xXWTUvhF9JEDimtL/5Q85zxfDk9ftaP6P7Bzzn8SSAwIw2++JFXO3aE82I/UHFuwNfB/lqp80P6w3IilO6kfmwfNAjuaSFpLsKbxJqYCfC/ygWB3M1yh8mnWSkqxzYC5dLz4MQ24IC5SS0qY3TKAG7H9wwoqy6mEw0vM/1oemVeD05/PE8gm/XRjnTyLyRF0u8DcGK0E6NOeZi0KZQnCxMkuWyg0Abvm8flLPuDM4nDL0pqJC4rWlVPJu2Hmh5rGjnAv/k0FkMdrNelzs/3UkhFabTnSW1RtdTweU/3";
    
    static private ScoreOperation[] createOperations()
    {
        final int length = 4*8;
        ScoreOperation[] operations = new ScoreOperation[100_000];
        
        for (int i = 0; i < operations.length; i++)
        {
            int a = i%(BIG_HASH.length()-length);
            String str = BIG_HASH.substring(a, a+length);
            long g = 0;
            byte[] bs = null;
            try
            {
                bs = Base64.getDecoder().decode(str);
                bs = Arrays.copyOfRange(bs, 0, 8);
                if (bs.length < 8) throw new RuntimeException("length=" + bs.length);
                g = ByteBuffer.wrap(bs).getLong();
            }
            catch (RuntimeException e)
            {
                System.out.println("g=" + g + " str=" + str);
                if (bs != null) System.out.println("bs.length=" + bs.length + " " + Arrays.toString(bs));
                throw e;
            }
            g = Math.abs(g);
            operations[i] = new ScoreOperation(NAMES[(int) (g%NAMES.length)], g%73);
        }
        
        return operations;
    }
    
    static private void run(String runId, ScoreOperation[] operations, Executor executor)
    {
        System.out.println(runId);
        long clock = System.currentTimeMillis();
        
        IScoreService service = resolveImplementation();
        
        for (ScoreOperation op: operations)
        {
            executor.execute(() ->
            {
                service.postScore(op.userId, op.points);
                service.retrieveScore(op.userId);
            });
        }
        
        executor.execute(() ->
        {
            service.retrieveRanking().stream().forEach(s -> System.out.println(s.ticker()));
            System.out.println("RAN in " + (System.currentTimeMillis()-clock) + " ms");
            System.out.println("------------------");
        });
    }
    
    static public void main(String args[])
    {
        int threads = Runtime.getRuntime().availableProcessors();
        System.out.println("using " + threads + " threads for async processing\n");
        
        ScoreOperation[] operations = createOperations();
        
        // sync validation (correctedness and time)
        run("SYNC OPERATIONS", operations, 
            r -> r.run());
        
        // async validation (correctedness and time)
        run("ASYNC OPERATIONS", operations, 
            Executors.newFixedThreadPool(threads));
    }
}
