import java.util.regex.*;
import java.util.Arrays;

public class PolynomialFunction implements ParametricFunction {

    private double[] coefficients;
    public String polynomialFunction;

    public PolynomialFunction(String polynomialString) {
        this.coefficients = new double[]{0};
        this.polynomialFunction = polynomialString;
        fromString(polynomialString);
    }

    public void fromString(String polynomial) {
        Pattern termPattern = Pattern.compile("([-+]?\\s*\\d*\\.?\\d*(?:/\\d+)*)?x(\\^(-?\\d+))?|([-+]?\\s*\\d+(/\\d+)?)");
        Matcher matcher = termPattern.matcher(polynomial);

        int maxExponent = 0;

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
                if (coefStr == null || coefStr.isEmpty()) {
                    coef = 1;
                } else if (coefStr.trim().equals("+")) {
                    coef = 1;
                } else if (coefStr.trim().equals("-")) {
                    coef = -1;
                } else {
                    coef = parseFraction(coefStr.trim());
                }

                if (expStr == null) {
                    exp = 1;
                } else {
                    exp = Integer.parseInt(expStr);
                }
            }

            maxExponent = Math.max(maxExponent, exp);

            // Resize the array if necessary
            if (exp >= this.coefficients.length) {
                this.coefficients = Arrays.copyOf(this.coefficients, exp + 1);
            }

            this.coefficients[exp] = coef;
        }
        this.coefficients = reverseArray(this.coefficients);
    }

    private double parseFraction(String input) {
        if (input.contains("/")) {
            String[] fractionParts = input.split("/");
            return Double.parseDouble(fractionParts[0]) / Double.parseDouble(fractionParts[1]);
        } else {
            return Double.parseDouble(input);
        }
    }

    public double[] reverseArray(double[] array) {
        int start = 0;
        int end = array.length - 1;

        while (start < end) {
            double temp = array[start];
            array[start] = array[end];
            array[end] = temp;

            start++;
            end--;
        }
        return array;
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
        double result = this.coefficients[0];
        for (int i = 1; i < this.coefficients.length; i++) {
            result = result * t + this.coefficients[i];
        }
        return new Vector2D(t, result);
    }
}
