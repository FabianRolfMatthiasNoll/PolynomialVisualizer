import java.util.*;
import java.util.regex.*;

public class Polynomial {

    public String polynomial;
    private static final double DEFAULT_START_X = -100.0;
    private static final double DEFAULT_END_X = 100.0;
    private static final double DEFAULT_STEP = 0.01;

    private static class Term {
        double coefficient;
        int exponent;

        Term(double coefficient, int exponent) {
            this.coefficient = coefficient;
            this.exponent = exponent;
        }
    }

    private final List<Term> terms;

    public Polynomial(String polynomial) {
        this.polynomial = polynomial;
        terms = extractTerms(polynomial);
        terms.sort(Comparator.comparingInt(t -> -t.exponent));
        fillMissingTerms();
    }

    private List<Term> extractTerms(String polynomial) {
        List<Term> terms = new ArrayList<>();
        Pattern termPattern = Pattern.compile("([-+]?\\s*\\d*\\.?\\d*)?x(\\^(-?\\d+))?|([-+]?\\s*\\d+)");
        Matcher matcher = termPattern.matcher(polynomial);

        while (matcher.find()) {
            String coefStr = matcher.group(1);
            String expStr = matcher.group(3);
            String constantStr = matcher.group(4);

            double coef;
            int exp;

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
        // I have to check the last term because there could be lower terms missing.
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

    public List<Double> getZeroPoints() {
        return getZeroPoints(DEFAULT_START_X, DEFAULT_END_X, DEFAULT_STEP);
    }

    public List<Double> getExtremePoints() {
        return getExtremePoints(DEFAULT_START_X, DEFAULT_END_X, DEFAULT_STEP);
    }

    public List<Double> getZeroPoints(double start, double end, double step) {
        List<Double> zeros = new ArrayList<>();
        double prevY = evaluate(start);
        for (double x = start + step; x <= end; x += step) {
            double y = evaluate(x);
            if (prevY * y <= 0) {
                zeros.add(x - step/2);
            }
            prevY = y;
        }
        return zeros;
    }

    public List<Double> getExtremePoints(double start, double end, double step) {
        List<Double> extremes = new ArrayList<>();
        double prevSlope = (evaluate(start + step) - evaluate(start)) / step;
        for (double x = start + 2*step; x <= end; x += step) {
            double slope = (evaluate(x) - evaluate(x - step)) / step;
            if (prevSlope * slope <= 0) { // changing slope direction
                extremes.add(x - step); // estimating extreme point
            }
            prevSlope = slope;
        }
        return extremes;
    }
}
