import java.util.*;
import java.util.regex.*;

public class Polynomial {

    private static class Term {
        double coefficient;
        int exponent;

        Term(double coefficient, int exponent) {
            this.coefficient = coefficient;
            this.exponent = exponent;
        }
    }

    private List<Term> terms;

    public Polynomial(String polynomial) {
        terms = extractTerms(polynomial);
        Collections.sort(terms, Comparator.comparingInt(t -> -t.exponent));
        fillMissingTerms();
    }

    private List<Term> extractTerms(String polynomial) {
        List<Term> terms = new ArrayList<>();
        Pattern termPattern = Pattern.compile("([-+]?\\s*\\d*\\.?\\d*)?x(\\^(\\-?\\d+))?|([-+]?\\s*\\d+)");
        Matcher matcher = termPattern.matcher(polynomial);

        while (matcher.find()) {
            String coefStr = matcher.group(1);
            String expStr = matcher.group(3);
            String constantStr = matcher.group(4);

            double coef = 1;
            int exp = 1;

            if (constantStr != null) {
                coef = Double.parseDouble(constantStr.trim());
                exp = 0;
            } else {
                if (coefStr == null) {
                    coef = 1;
                } else if (coefStr.trim().equals("+")) {
                    coef = 1;
                } else if (coefStr.trim().equals("-")) {
                    coef = -1;
                } else {
                    coef = Double.parseDouble(coefStr.trim());
                }

                if (expStr == null) {
                    exp = 1;
                } else {
                    exp = Integer.parseInt(expStr);
                }
            }

            terms.add(new Term(coef, exp));
        }

        return terms;
    }

    private void fillMissingTerms() {

        for (int i = 0; i < terms.size() - 1; i++) {
            int diff = terms.get(i).exponent - terms.get(i + 1).exponent;
            if (diff > 1) {
                for (int j = 1; j < diff; j++) {
                    terms.add(i + j, new Term(0, terms.get(i).exponent - j));
                }
            }
        }
        // i have to check the last term because there could be lower terms missing.
        // for example: 3x^2 is the last one then x^1 and x^0 are missing
        if (terms.get(terms.size() - 1).exponent > 0) {
            for (int i = terms.get(terms.size() - 1).exponent - 1; i >= 0; i--) {
                terms.add(new Term(0, i));
            }
        }
    }

    public double evaluate(double x) {
        double result = terms.get(0).coefficient;
        for (int i = 1; i < terms.size(); i++) {
            result = result * x + terms.get(i).coefficient;
        }
        return result;
    }
}
