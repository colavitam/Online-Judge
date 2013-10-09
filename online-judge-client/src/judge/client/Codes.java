package judge.client;

public interface Codes {
    public static int JUDGING_INIT = 1;
    public static int COMPILE_PASS = 2;
    public static int COMPILE_FAIL = 3;
    public static int TEST_PASS = 4;
    public static int TEST_FAIL_WRONG = 5;
    public static int TEST_FAIL_TIMEOUT = 6;
    public static int TESTS_BAD = 7;
    public static int TESTS_GOOD = 8;
    public static int INVALID_PROBLEM = 9;
    public static int JUDGING_ERROR = 10;
    public static int JUDGING_ABORT = 0;
    public final static String SERVERIPADDR = "74.104.179.243";
}
