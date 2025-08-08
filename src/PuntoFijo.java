import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

/**
 * Una clase de utilidad para encontrar raíces de una función utilizando el método del punto fijo.
 * El método del punto fijo busca soluciones a la ecuación x = g(x).
 */
public class PuntoFijo {

    // Caché para evaluaciones de función para evitar cálculos redundantes
    private final Map<Double, Double> evaluationCache = new HashMap<>();
    private final DoubleUnaryOperator function;
    private final double initialGuess;
    private final int maxIterations;
    private final double tolerance;

    // Para almacenar los datos de las iteraciones
    private final List<IterationData> iterationHistory = new ArrayList<>();

    /**
     * Clase para almacenar los datos de cada iteración
     */
    public static class IterationData {
        private final int iteration;
        private final double xValue;
        private final double gxValue;
        private final double error;

        public IterationData(int iteration, double xValue, double gxValue, double error) {
            this.iteration = iteration;
            this.xValue = xValue;
            this.gxValue = gxValue;
            this.error = error;
        }

        public int getIteration() {
            return iteration;
        }

        public double getXValue() {
            return xValue;
        }

        public double getGxValue() {
            return gxValue;
        }

        public double getError() {
            return error;
        }
    }

    /**
     * Construye un FixedPointRootFinder con los parámetros especificados.
     *
     * @param function      La función g(x) cuyo punto fijo se debe encontrar
     * @param initialGuess  El valor inicial de x
     * @param maxIterations El número máximo de iteraciones
     * @param tolerance     La tolerancia de error para la aproximación
     * @throws IllegalArgumentException si el número de iteraciones no es positivo o la tolerancia no es positiva
     */
    public PuntoFijo(DoubleUnaryOperator function, double initialGuess, int maxIterations, double tolerance) {
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("El número de iteraciones debe ser positivo");
        }

        if (tolerance <= 0) {
            throw new IllegalArgumentException("La tolerancia debe ser positiva");
        }

        this.function = function;
        this.initialGuess = initialGuess;
        this.maxIterations = maxIterations;
        this.tolerance = tolerance;
    }

    /**
     * Evalúa la función en un punto, utilizando caché para mayor eficiencia.
     *
     * @param x El punto en el que evaluar la función
     * @return El valor de la función en x
     */
    private double evaluateFunction(double x) {
        // Redondear a una precisión razonable para mejorar los aciertos de caché
        double roundedX = Math.round(x / (tolerance * 0.01)) * (tolerance * 0.01);

        // Verificar si ya calculamos este valor
        if (evaluationCache.containsKey(roundedX)) {
            return evaluationCache.get(roundedX);
        }

        // Calcular el valor y almacenarlo en caché
        double result = function.applyAsDouble(roundedX);
        evaluationCache.put(roundedX, result);
        return result;
    }

    /**
     * Encuentra un punto fijo de la función g(x) comenzando desde el valor inicial.
     * Un punto fijo es un valor x tal que x = g(x).
     *
     * @return El punto fijo aproximado de la función
     */
    public double findFixedPoint() {
        double x = initialGuess;
        double nextX;

        for (int i = 1; i <= maxIterations; i++) {
            nextX = evaluateFunction(x);

            // Calcular error como la diferencia entre x y g(x)
            double error = Math.abs(nextX - x);

            // Registrar esta iteración
            iterationHistory.add(new IterationData(i, x, nextX, error));

            // Verificar convergencia
            if (error < tolerance) {
                break;
            }

            // Actualizar x para la siguiente iteración
            x = nextX;
        }

        return x;
    }

    /**
     * Obtiene el historial de iteraciones.
     *
     * @return Una lista con los datos de cada iteración
     */
    public List<IterationData> getIterationHistory() {
        return iterationHistory;
    }

    /**
     * Crea una función a partir de una expresión matemática proporcionada por el usuario.
     * Soporta operaciones aritméticas básicas y funciones matemáticas comunes.
     *
     * @param expression La expresión matemática en términos de x
     * @return Un DoubleUnaryOperator que representa la función
     * @throws IllegalArgumentException si la expresión no puede ser analizada
     */
    public static DoubleUnaryOperator parseFunction(String expression) {
        // Utilizar el mismo parser que BinarySearchRootFinder
        return Biseccion.parseFunction(expression);
    }
}