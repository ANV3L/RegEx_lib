import com.kirusha.regex.Regex;

class Main {

    public static boolean test_(String pattern, String s) {
        return Regex.compile(pattern).matches(s);
    }

    public static void test(String pattern, String s) {
        boolean result = test_(pattern, s);
        System.out.println("Pattern: " + pattern + ", String: " + s + ", Matches: " + result);
    }

    public static void main(String[] args) {
        // test("[a-c]", "b");
        test("a(a|b)#a(b|c)", "aa");
        test("a(a|b)#a(b|c)", "ab");
        test("a#b#c", "a");
        test("(a#b)#c", "b");
    }
}