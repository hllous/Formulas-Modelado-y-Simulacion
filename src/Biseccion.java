import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

/**
 * Una clase de utilidad para encontrar raíces de una función utilizando el método de búsqueda binaria.
 * Optimizada para eficiencia y robustez.
 */
public class Biseccion {

    // Caché para evaluaciones de función para evitar cálculos redundantes
    private final Map<Double, Double> evaluationCache = new HashMap<>();
    private final DoubleUnaryOperator function;
    private final double a;
    private final double b;
    private final double tolerance;
    private final int maxIterations;

    // Para almacenar los datos de las iteraciones
    private final List<IterationData> iterationHistory = new ArrayList<>();

    /**
     * Clase para almacenar los datos de cada iteración
     */
    public static class IterationData {
        private final int iteration;
        private final double a;
        private final double b;
        private final double c;
        private final double fc;
        private final double error;

        public IterationData(int iteration, double a, double b, double c, double fc, double error) {
            this.iteration = iteration;
            this.a = a;
            this.b = b;
            this.c = c;
            this.fc = fc;
            this.error = error;
        }

        public int getIteration() {
            return iteration;
        }

        public double getA() {
            return a;
        }

        public double getB() {
            return b;
        }

        public double getC() {
            return c;
        }

        public double getFc() {
            return fc;
        }

        public double getError() {
            return error;
        }
    }

    /**
     * Construye un BinarySearchRootFinder con los parámetros especificados.
     *
     * @param function   La función cuya raíz se debe encontrar
     * @param a          El límite inferior del intervalo
     * @param b          El límite superior del intervalo
     * @param tolerance  La tolerancia de error para la aproximación
     * @throws IllegalArgumentException si la tolerancia no es positiva
     */
    public Biseccion(DoubleUnaryOperator function, double a, double b, double tolerance) {
        if (tolerance <= 0) {
            throw new IllegalArgumentException("La tolerancia debe ser positiva");
        }

        this.function = function;
        this.a = a;
        this.b = b;
        this.tolerance = tolerance;

        // Calcular el número máximo de iteraciones basado en el tamaño del intervalo y la tolerancia
        // Esto asegura que el algoritmo termine incluso si surgen problemas de precisión de punto flotante
        this.maxIterations = (int) Math.ceil(Math.log((b - a) / tolerance) / Math.log(2)) + 10; // Agregar margen de seguridad
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
     * Encuentra una raíz de la función dentro del intervalo [a, b] con la tolerancia especificada.
     *
     * @return La raíz aproximada de la función
     * @throws IllegalArgumentException si la función no tiene un cambio de signo en el intervalo
     */
    public double findRoot() {
        double left = a;
        double right = b;

        // Verificar si los valores de la función en los puntos extremos tienen signos opuestos
        double fLeft = evaluateFunction(left);
        double fRight = evaluateFunction(right);

        // Verificar raíces exactas en los puntos extremos
        if (Math.abs(fLeft) < tolerance) return left;
        if (Math.abs(fRight) < tolerance) return right;

        if (fLeft * fRight >= 0) {
            throw new IllegalArgumentException(
                    "La función debe tener signos opuestos en los extremos del intervalo: f(" + left + ") = " + fLeft +
                            ", f(" + right + ") = " + fRight);
        }

        double mid = 0, fMid = 0;
        int iterations = 0;

        // Ciclo de búsqueda binaria con límite de iteraciones
        while ((right - left) > tolerance && iterations < maxIterations) {
            iterations++;

            // Calcular punto medio utilizando un método numéricamente más estable
            mid = left + (right - left) / 2.0;
            fMid = evaluateFunction(mid);

            // Registrar esta iteración
            double error = right - left;
            iterationHistory.add(new IterationData(iterations, left, right, mid, fMid, error));

            // Si encontramos raíz exacta
            if (Math.abs(fMid) < tolerance) {
                break;
            }

            // Decidir con qué mitad continuar
            if (fLeft * fMid < 0) {
                // La raíz está en la mitad izquierda
                right = mid;
                fRight = fMid;
            } else {
                // La raíz está en la mitad derecha
                left = mid;
                fLeft = fMid;
            }
        }

        // Retornar el punto medio del intervalo final
        return mid;
    }

    /**
     * Obtiene el historial de iteraciones.
     *
     * @return Una lista con los datos de cada iteración
     */
    public List<IterationData> getIterationHistory() {
        return iterationHistory;
    }

    // El resto del código se mantiene igual...

    /**
     * Crea una función a partir de una expresión matemática proporcionada por el usuario.
     * Soporta operaciones aritméticas básicas y funciones matemáticas comunes.
     *
     * @param expression La expresión matemática en términos de x
     * @return Un DoubleUnaryOperator que representa la función
     * @throws IllegalArgumentException si la expresión no puede ser analizada
     */
    public static DoubleUnaryOperator parseFunction(String expression) {
        // Normalizar la expresión para funciones comunes
        String normalizedExpression = expression.trim().toLowerCase();

        // Manejar casos simples directamente usando expresiones lambda para mayor eficiencia
        switch (normalizedExpression) {
            case "sin(x)": return Math::sin;
            case "cos(x)": return Math::cos;
            case "tan(x)": return Math::tan;
            case "exp(x)":
            case "e^x": return Math::exp;
            case "log(x)":
            case "ln(x)": return Math::log;
            case "sqrt(x)": return Math::sqrt;
            case "x^2":
            case "x*x": return x -> x * x;
            case "x^3": return x -> x * x * x;
            case "x^2-4":
            case "x*x-4": return x -> x * x - 4;
            case "x^3-x-2": return x -> x * x * x - x - 2;
            case "cos(x)-x": return x -> Math.cos(x) - x;
            case "2*e^(x^2)-5*x":
            case "2*exp(x^2)-5*x": return x -> 2 * Math.exp(x * x) - 5 * x;
        }

        // Para otras expresiones, crear una función que analice la expresión en cada llamada
        return new ExpressionEvaluator(normalizedExpression);
    }

    /**
     * Una clase especializada para evaluación eficiente de expresiones.
     * Implementa DoubleUnaryOperator para evaluación de funciones.
     */
    private static class ExpressionEvaluator implements DoubleUnaryOperator {
        private final String expression;
        private final Map<Double, Double> cache = new HashMap<>();
        private static final double CACHE_PRECISION = 1e-10;

        public ExpressionEvaluator(String expression) {
            this.expression = expression;
        }

        @Override
        public double applyAsDouble(double x) {
            // Redondear a una precisión adecuada para caché
            double roundedX = Math.round(x / CACHE_PRECISION) * CACHE_PRECISION;

            // Verificar caché primero
            if (cache.containsKey(roundedX)) {
                return cache.get(roundedX);
            }

            // Calcular el resultado
            double result = evaluateExpression(expression, x);

            // Almacenar el resultado en caché
            cache.put(roundedX, result);
            return result;
        }

        /**
         * Evalúa una expresión matemática para un valor específico de x.
         * Este método utiliza un enfoque de descenso recursivo para mayor eficiencia.
         */
        private double evaluateExpression(String expr, double x) {
            // Reemplazar variables y constantes
            expr = expr.replace("x", "(" + x + ")")
                    .replace("e", String.valueOf(Math.E))
                    .replace("pi", String.valueOf(Math.PI));

            // Manejar funciones primero
            expr = evaluateFunctions(expr);

            // Manejar operadores en orden de precedencia
            return evaluateOperators(expr);
        }

        /**
         * Evaluar todas las funciones en la expresión
         */
        private String evaluateFunctions(String expr) {
            // Manejar funciones matemáticas comunes
            while (true) {
                // Encontrar la primera llamada a función
                int funcStart = findFirstFunction(expr);
                if (funcStart == -1) break;

                // Extraer nombre de función
                int nameStart = funcStart;
                while (nameStart > 0 && Character.isLetter(expr.charAt(nameStart - 1))) {
                    nameStart--;
                }
                String funcName = expr.substring(nameStart, funcStart);

                // Encontrar paréntesis coincidente
                int openParen = expr.indexOf('(', funcStart);
                int closeParen = findMatchingCloseParen(expr, openParen);

                if (closeParen == -1) {
                    throw new IllegalArgumentException("Falta paréntesis de cierre para la función: " + funcName);
                }

                // Extraer y evaluar el argumento
                String arg = expr.substring(openParen + 1, closeParen);
                double argValue = evaluateOperators(arg);

                // Aplicar la función
                double result;
                switch (funcName) {
                    case "sin": result = Math.sin(argValue); break;
                    case "cos": result = Math.cos(argValue); break;
                    case "tan": result = Math.tan(argValue); break;
                    case "sqrt": result = Math.sqrt(argValue); break;
                    case "log":
                    case "ln": result = Math.log(argValue); break;
                    case "exp": result = Math.exp(argValue); break;
                    default: throw new IllegalArgumentException("Función desconocida: " + funcName);
                }

                // Reemplazar la llamada a función con su resultado
                expr = expr.substring(0, nameStart) + result + expr.substring(closeParen + 1);
            }

            return expr;
        }

        /**
         * Encontrar el índice de la primera llamada a función en una expresión
         */
        private int findFirstFunction(String expr) {
            String[] functions = {"sin", "cos", "tan", "sqrt", "log", "ln", "exp"};
            int earliest = Integer.MAX_VALUE;

            for (String func : functions) {
                int index = expr.indexOf(func + "(");
                if (index != -1 && index < earliest) {
                    earliest = index + func.length();
                }
            }

            return (earliest == Integer.MAX_VALUE) ? -1 : earliest;
        }

        /**
         * Evaluar operadores en una expresión según reglas de precedencia
         */
        private double evaluateOperators(String expr) {
            // Manejar paréntesis primero
            while (true) {
                int openParen = expr.indexOf('(');
                if (openParen == -1) break;

                int closeParen = findMatchingCloseParen(expr, openParen);
                if (closeParen == -1) {
                    throw new IllegalArgumentException("Falta paréntesis de cierre en: " + expr);
                }

                String subExpr = expr.substring(openParen + 1, closeParen);
                double subResult = evaluateOperators(subExpr);

                expr = expr.substring(0, openParen) + subResult + expr.substring(closeParen + 1);
            }

            // Manejar operaciones de potencia (^)
            while (expr.contains("^")) {
                int opIdx = expr.indexOf('^');
                String leftExpr = extractLeftOperand(expr, opIdx);
                String rightExpr = extractRightOperand(expr, opIdx);

                double leftVal = Double.parseDouble(leftExpr);
                double rightVal = Double.parseDouble(rightExpr);
                double result = Math.pow(leftVal, rightVal);

                expr = expr.substring(0, opIdx - leftExpr.length()) + result +
                        expr.substring(opIdx + 1 + rightExpr.length());
            }

            // Manejar multiplicación y división
            while (expr.contains("*") || expr.contains("/")) {
                int mulIdx = expr.indexOf('*');
                int divIdx = expr.indexOf('/');

                int opIdx;
                char op;

                if (mulIdx == -1) {
                    opIdx = divIdx;
                    op = '/';
                } else if (divIdx == -1) {
                    opIdx = mulIdx;
                    op = '*';
                } else {
                    opIdx = Math.min(mulIdx, divIdx);
                    op = (opIdx == mulIdx) ? '*' : '/';
                }

                String leftExpr = extractLeftOperand(expr, opIdx);
                String rightExpr = extractRightOperand(expr, opIdx);

                double leftVal = Double.parseDouble(leftExpr);
                double rightVal = Double.parseDouble(rightExpr);

                double result;
                if (op == '*') {
                    result = leftVal * rightVal;
                } else {
                    if (rightVal == 0) {
                        throw new ArithmeticException("División por cero");
                    }
                    result = leftVal / rightVal;
                }

                expr = expr.substring(0, opIdx - leftExpr.length()) + result +
                        expr.substring(opIdx + 1 + rightExpr.length());
            }

            // Manejar suma y resta
            while (true) {
                // Encontrar primer + o - que no esté al principio
                int addIdx = -1;
                int subIdx = -1;

                for (int i = 1; i < expr.length(); i++) {
                    if (expr.charAt(i) == '+' && addIdx == -1) {
                        addIdx = i;
                    } else if (expr.charAt(i) == '-' && subIdx == -1 &&
                            (Character.isDigit(expr.charAt(i-1)) || expr.charAt(i-1) == ')')) {
                        subIdx = i;
                    }
                }

                if (addIdx == -1 && subIdx == -1) break;

                int opIdx = (addIdx == -1) ? subIdx : (subIdx == -1) ? addIdx : Math.min(addIdx, subIdx);
                char op = expr.charAt(opIdx);

                String leftExpr = expr.substring(0, opIdx).trim();
                String rightExpr = expr.substring(opIdx + 1).trim();

                if (leftExpr.isEmpty()) {
                    // Manejar caso donde - está al principio (número negativo)
                    return Double.parseDouble(expr);
                }

                double leftVal, rightVal;
                try {
                    leftVal = Double.parseDouble(leftExpr);
                    rightVal = Double.parseDouble(rightExpr);
                } catch (NumberFormatException e) {
                    // Si no podemos analizar como doubles, puede haber más operaciones para procesar
                    // Procesar rightExpr primero
                    rightVal = evaluateOperators(rightExpr);
                    try {
                        leftVal = Double.parseDouble(leftExpr);
                    } catch (NumberFormatException e2) {
                        leftVal = evaluateOperators(leftExpr);
                    }
                }

                return (op == '+') ? leftVal + rightVal : leftVal - rightVal;
            }

            // Si no quedan operadores, es solo un número
            try {
                return Double.parseDouble(expr.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("No se puede evaluar la expresión: " + expr);
            }
        }

        /**
         * Encontrar el paréntesis de cierre coincidente
         */
        private int findMatchingCloseParen(String expr, int openParenIdx) {
            int count = 1;
            for (int i = openParenIdx + 1; i < expr.length(); i++) {
                if (expr.charAt(i) == '(') count++;
                else if (expr.charAt(i) == ')') count--;

                if (count == 0) return i;
            }
            return -1;
        }

        /**
         * Extraer el operando izquierdo de un operador binario
         */
        private String extractLeftOperand(String expr, int opIdx) {
            int start = opIdx - 1;

            // Omitir espacios en blanco
            while (start >= 0 && Character.isWhitespace(expr.charAt(start))) start--;

            // Si es un paréntesis de cierre, extraer toda la expresión entre paréntesis
            if (start >= 0 && expr.charAt(start) == ')') {
                int openParen = findMatchingOpenParen(expr, start);
                if (openParen >= 0) {
                    return expr.substring(openParen, start + 1);
                }
            }

            // De lo contrario, extraer el número
            int end = start;
            while (start >= 0 && (Character.isDigit(expr.charAt(start)) ||
                    expr.charAt(start) == '.' ||
                    (start > 0 && expr.charAt(start) == 'E') ||
                    (expr.charAt(start) == '-' && start > 0 &&
                            (expr.charAt(start-1) == 'E' || expr.charAt(start-1) == 'e')))) {
                start--;
            }

            // Manejar números negativos
            if (start >= 0 && expr.charAt(start) == '-') {
                // Verificar si este es un menos unario
                if (start == 0 || isOperator(expr.charAt(start-1)) || expr.charAt(start-1) == '(') {
                    start--;
                }
            }

            return expr.substring(start + 1, end + 1);
        }

        /**
         * Encontrar el paréntesis de apertura coincidente, buscando hacia atrás
         */
        private int findMatchingOpenParen(String expr, int closeParenIdx) {
            int count = 1;
            for (int i = closeParenIdx - 1; i >= 0; i--) {
                if (expr.charAt(i) == ')') count++;
                else if (expr.charAt(i) == '(') count--;

                if (count == 0) return i;
            }
            return -1;
        }

        /**
         * Extraer el operando derecho de un operador binario
         */
        private String extractRightOperand(String expr, int opIdx) {
            int start = opIdx + 1;

            // Omitir espacios en blanco
            while (start < expr.length() && Character.isWhitespace(expr.charAt(start))) start++;

            // Manejar números negativos
            if (start < expr.length() && expr.charAt(start) == '-') {
                start++;
            }

            // Si es un paréntesis de apertura, extraer toda la expresión entre paréntesis
            if (start < expr.length() && expr.charAt(start) == '(') {
                int closeParen = findMatchingCloseParen(expr, start);
                if (closeParen >= 0) {
                    return expr.substring(start, closeParen + 1);
                }
            }

            // De lo contrario, extraer el número
            int end = start;
            while (end < expr.length() && (Character.isDigit(expr.charAt(end)) ||
                    expr.charAt(end) == '.' ||
                    (end > start && (expr.charAt(end) == 'E' || expr.charAt(end) == 'e')) ||
                    (expr.charAt(end) == '+' || expr.charAt(end) == '-') &&
                            end > start && (expr.charAt(end-1) == 'E' || expr.charAt(end-1) == 'e'))) {
                end++;
            }

            return expr.substring(start, end);
        }

        /**
         * Verificar si un carácter es un operador
         */
        private boolean isOperator(char c) {
            return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
        }
    }
}