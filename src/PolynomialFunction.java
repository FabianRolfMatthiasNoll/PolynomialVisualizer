import java.util.regex.*;
import java.util.Arrays;
import java.util.*;

public class PolynomialFunction implements ParametricFunction {

    private double[] coefficients;
    public String polynomialFunction;
    private static final double DEFAULT_START_X = -100.0;
    private static final double DEFAULT_END_X = 100.0;
    private static final double DEFAULT_STEP = 0.01;

    public PolynomialFunction(String polynomialString) {
        this.coefficients = new double[]{0};
        this.polynomialFunction = polynomialString;
        fromString(polynomialString);
    }

    public void fromString(String polynomial) {
        Pattern termPattern = Pattern.compile("([-+]?\\s*\\d*\\.?\\d*(?:/\\d+)*)?x(\\^(-?\\d+))?|([-+]?\\s*\\d+(/\\d+)?)");
        Matcher matcher = termPattern.matcher(polynomial);

        while (matcher.find()) {
            String coefStr = matcher.group(1);
            String expStr = matcher.group(3);
            String constantStr = matcher.group(4);

            double coef;
            int exp;

            if (constantStr != null && !constantStr.isEmpty()) {
                coef = parseFraction(constantStr.trim());
                exp = 0;
            } else {
                coef = 1;
                if (coefStr != null && !coefStr.isEmpty()) {
                    if (coefStr.trim().equals("+") || coefStr.trim().equals("-")) {
                        coef = coefStr.trim().equals("+") ? 1 : -1;
                    } else {
                        coef = parseFraction(coefStr.trim());
                    }
                }

                exp = (expStr == null) ? 1 : Integer.parseInt(expStr);
            }

            if (exp >= this.coefficients.length) {
                this.coefficients = Arrays.copyOf(this.coefficients, exp + 1);
            }

            this.coefficients[exp] = coef;
        }
    }

    private double parseFraction(String input) {
        if (input.contains("/")) {
            String[] fractionParts = input.split("/");
            return Double.parseDouble(fractionParts[0]) / Double.parseDouble(fractionParts[1]);
        } else {
            return Double.parseDouble(input);
        }
    }

    public int degree() {
        for (int i = coefficients.length - 1; i >= 0; i--) {
            double coefficient = coefficients[i];
            if (coefficient != 0.0) {
                return i;
            }
        }

        return 0;
    }

    @Override
    public Vector2D evaluate(double t) {
        // Horner scheme
        double result = 0;
        for (int i = this.coefficients.length - 1; i >= 0; i--) {
            result = result * t + this.coefficients[i];
        }
        return new Vector2D(t, result);
    }

    public void derive() {
        if (coefficients == null || coefficients.length == 0) {
            return;
        }

        for (int i = 1; i < coefficients.length; i++) {
            coefficients[i - 1] = i * coefficients[i];
        }

        coefficients[coefficients.length - 1] = 0;

        StringBuilder sb = new StringBuilder();
        for (int i = coefficients.length - 1; i >= 0; i--) {
            double coefficient = coefficients[i];
            if (coefficient != 0) {
                if (coefficient > 0 && sb.length() > 0) {
                    sb.append("+");
                }
                if (i > 0) {
                    sb.append(String.format("%.2fx^%d", coefficient, i));
                } else {
                    sb.append(String.format("%.2f", coefficient));
                }
            }
        }
        polynomialFunction =  sb.toString();
    }

    public List<Double> getZeroPoints() {
        return getZeroPoints(DEFAULT_START_X, DEFAULT_END_X, DEFAULT_STEP);
    }

    public List<Double> getExtremePoints() {
        return getExtremePoints(DEFAULT_START_X, DEFAULT_END_X, DEFAULT_STEP);
    }

    public List<Double> getZeroPoints(double start, double end, double step) {
        List<Double> zeros = new ArrayList<>();
        Vector2D prevPoint = evaluate(start);
        for (double x = start + step; x <= end; x += step) {
            Vector2D point = evaluate(x);
            if (prevPoint.y * point.y <= 0) {
                zeros.add(x - step / 2);
            }
            prevPoint = point;
        }
        return zeros;
    }

    public List<Double> getExtremePoints(double start, double end, double step) {
        List<Double> extremes = new ArrayList<>();
        Vector2D startPoint = evaluate(start);
        Vector2D nextPoint = evaluate(start + step);
        double prevSlope = (nextPoint.y - startPoint.y) / step;
        for (double x = start + 2 * step; x <= end; x += step) {
            Vector2D point = evaluate(x);
            double slope = (point.y - evaluate(x - step).y) / step;
            if (prevSlope * slope <= 0) {
                extremes.add(x - step);
            }
            prevSlope = slope;
        }
        return extremes;
    }
}
