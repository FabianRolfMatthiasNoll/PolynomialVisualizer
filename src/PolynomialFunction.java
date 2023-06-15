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
                coef = 1; // default coefficient
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

        // Update coefficients and decrease exponent
        for (int i = 1; i < coefficients.length; i++) {
            coefficients[i - 1] = i * coefficients[i];
        }

        // Remove last element (it will always be 0 after derivative)
        coefficients[coefficients.length - 1] = 0;

        // Construct the polynomial string
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
}
