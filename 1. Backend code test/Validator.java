package tester.hr.validation;

import tester.hr.interfaces.IScore;
import tester.hr.interfaces.IScoreService;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

abstract public class Validator
{
    static private IScoreService resolveImplementation()
    {
        // TODO return your ScoreService implementation
//        return new ScoreService();
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

    static Map<String, Long> pointsMap = new HashMap<>(); //validation map
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
            String name = NAMES[(int) (g%NAMES.length)];
            long points = g%73;
            operations[i] = new ScoreOperation(name, points);
            pointsMap.merge(name, points, Long::sum);
        }

        return operations;
    }

    static private void run(ScoreOperation[] operations, Executor executor, int seed) throws InterruptedException {
        IScoreService service = resolveImplementation();

        List<Runnable> allOperations = new ArrayList<>();

        int count = 0;
        CountDownLatch latch = new CountDownLatch(200_999);
        for (ScoreOperation op: operations)
        {
            allOperations.add(() -> {
                service.postScore(op.userId, op.points);
                latch.countDown();
            });
            allOperations.add(() -> {
                service.retrieveScore(op.userId);
                latch.countDown();
            });
            if (++count % 100 == 0) allOperations.add(() -> {
                List<IScore> iScores = service.retrieveRanking();
                checkRanking(iScores);
                latch.countDown();
            });
        }

        Collections.shuffle(allOperations, new Random(seed));
        allOperations.forEach(executor::execute);

        latch.await();

        service.retrieveRanking().forEach(s -> {
//            System.out.println(s.ticker() + " " + pointsMap.get(s.getUserId()));
            if (s.getScore() != pointsMap.get(s.getUserId())) throw new RuntimeException("Final ranking error");
        });
    }

    static private void checkRanking(List<IScore> iScores)
    {
        int prevPos = 0;
        long prevScore = Long.MAX_VALUE;
        for (IScore score : iScores)
        {
            int currPos = score.getPosition();
            long currScore = score.getScore();
            if (currPos < prevPos) throw new RuntimeException("Ranking position error");
            if (currScore > prevScore) throw new RuntimeException("Ranking score error");
            if (currPos == prevPos && currScore != prevScore) throw new RuntimeException("Ranking tie error");
            if (!(currPos == prevPos+1 || currPos == prevPos)) throw new RuntimeException("Ranking order error "+ currPos + " " + prevPos);
            prevPos = score.getPosition();
            prevScore = score.getScore();
        }
    }

    static public void main(String args[]) throws InterruptedException {
        int threads = 200; //Runtime.getRuntime().availableProcessors();
        int seeds = 30;
        System.out.println("using " + threads + " threads for async processing\n");

        ScoreOperation[] operations = createOperations();

        // sync validation (correctness and time)
        long clock = System.nanoTime();
        for (int seed = 1; seed <= seeds; seed++) {
            run(operations, Runnable::run, seed);
        }
        System.out.println("RAN SYNC in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-clock) + " ms");
        System.out.println("------------------");

        ExecutorService executorService = Executors.newFixedThreadPool(threads);

        // async validation (correctness and time)
        clock = System.nanoTime();
        for (int seed = 1; seed <= seeds; seed++) {
            run(operations, executorService, seed);
        }
        System.out.println("RAN ASYNC in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-clock) + " ms");
        System.out.println("------------------");
    }
}
