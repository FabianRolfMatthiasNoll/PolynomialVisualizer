import java.util.List;

public interface ParametricFunction {
    public Vector2D evaluate(double t);
    public void derive();
    public List<Double> getZeroPoints();
    public List<Double> getExtremePoints();
    public String getFunctionString();
}

