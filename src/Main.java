import java.util.List;
import java.util.Scanner;
import java.util.function.DoubleUnaryOperator;

/**
 * Clase principal para el buscador de raíces.
 * Esta clase solo maneja la entrada y salida del usuario.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Buscador de Raíces - Métodos Numéricos");
        System.out.println("=====================================");

        while (true) {
            try {
                // Seleccionar método
                System.out.println("\nSeleccione el método a utilizar:");
                System.out.println("1. Método de Bisección");
                System.out.println("2. Método de Punto Fijo");
                System.out.println("0. Salir");

                System.out.print("\nOpción: ");
                String option = scanner.nextLine();

                if (option.equals("0")) {
                    break;
                } else if (option.equals("1")) {
                    runBisectionMethod(scanner);
                } else if (option.equals("2")) {
                    runFixedPointMethod(scanner);
                } else {
                    System.out.println("Opción inválida. Por favor seleccione 0, 1 o 2.");
                }

            } catch (Exception e) {
                System.err.println("Error: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido"));
                // No limpiar el buffer del scanner aquí para evitar saltarse entradas
            }
        }

        System.out.println("Programa terminado.");
        scanner.close();
    }

    /**
     * Ejecuta el método de bisección.
     *
     * @param scanner El scanner para la entrada del usuario
     */
    private static void runBisectionMethod(Scanner scanner) {
        System.out.println("\nMétodo de Bisección");
        System.out.println("==================");

        // Ingresar una función personalizada
        System.out.println("\nIngrese una función matemática en términos de x (o 'volver' para regresar al menú principal):");
        System.out.println("Ejemplos: x^3 - x - 2, cos(x), cos(x) - x, x^2 - 4, sin(x), exp(x) - 5");
        System.out.println("Operaciones soportadas: +, -, *, /, ^");
        System.out.println("Funciones soportadas: sin, cos, tan, exp, log, sqrt");
        System.out.println("Constantes soportadas: e, pi");

        System.out.print("\nFunción: ");
        String customFunction = scanner.nextLine();

        if (customFunction.trim().equalsIgnoreCase("volver")) {
            return;
        }

        if (customFunction.trim().isEmpty()) {
            System.out.println("La función no puede estar vacía. Por favor intente de nuevo.");
            return;
        }

        DoubleUnaryOperator function;
        try {
            function = Biseccion.parseFunction(customFunction);

            // Probar la función para asegurarse de que funciona, pero sin imprimir resultados
            function.applyAsDouble(0);
            function.applyAsDouble(1);
            function.applyAsDouble(2);
        } catch (Exception e) {
            System.out.println("Error al analizar la función: " + e.getMessage());
            System.out.println("Por favor intente de nuevo con una expresión diferente.");
            return;
        }

        // Obtener los límites del intervalo
        double a, b, tolerance;

        System.out.print("\nIngrese el límite inferior del intervalo (a): ");
        try {
            a = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida para el límite inferior. Por favor ingrese un número válido.");
            return;
        }

        System.out.print("Ingrese el límite superior del intervalo (b): ");
        try {
            b = Double.parseDouble(scanner.nextLine());

            // Validar intervalo
            if (b <= a) {
                System.out.println("Error: El límite superior debe ser mayor que el límite inferior.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida para el límite superior. Por favor ingrese un número válido.");
            return;
        }

        // Obtener tolerancia
        System.out.print("Ingrese la tolerancia de error (debe ser positiva): ");
        try {
            tolerance = Double.parseDouble(scanner.nextLine());

            // Validar tolerancia
            if (tolerance <= 0) {
                System.out.println("Error: La tolerancia debe ser positiva.");
                return;
            }

            // Verificar si la tolerancia es demasiado pequeña para el intervalo
            double intervalSize = b - a;
            if (tolerance < intervalSize * Math.ulp(1.0)) {
                System.out.println("Advertencia: La tolerancia es muy pequeña en relación al tamaño del intervalo.");
                System.out.println("Debido a los límites de precisión de punto flotante, los resultados pueden no ser precisos.");
                System.out.println("Considere usar una tolerancia mayor o un intervalo más pequeño.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida para la tolerancia. Por favor ingrese un número válido.");
            return;
        }

        // Probar la función en los puntos extremos
        System.out.println("\nValores de la función en los extremos del intervalo:");
        double fa = function.applyAsDouble(a);
        double fb = function.applyAsDouble(b);
        System.out.println("f(" + a + ") = " + fa);
        System.out.println("f(" + b + ") = " + fb);

        // Verificar si los puntos extremos tienen signos opuestos
        if (fa * fb >= 0) {
            System.out.println("\nAdvertencia: La función no tiene signos opuestos en los extremos del intervalo.");
            System.out.println("El método de búsqueda binaria requiere que f(a) y f(b) tengan signos opuestos.");

            // Sugerir ajuste de intervalo si ambos valores son distintos de cero
            if (fa != 0 && fb != 0) {
                // Si ambos son positivos, buscar un cruce por cero
                if (fa > 0 && fb > 0) {
                    System.out.println("Ambos valores son positivos. Intente usar un valor menor para 'a'.");
                }
                // Si ambos son negativos, buscar un cruce por cero
                else if (fa < 0 && fb < 0) {
                    System.out.println("Ambos valores son negativos. Intente usar un valor mayor para 'b'.");
                }
            }

            System.out.println("¿Desea continuar de todos modos? (s/n)");
            String answer = scanner.nextLine();
            if (!answer.equalsIgnoreCase("s")) {
                return;
            }
        }

        // Crear buscador de raíces y encontrar raíz
        try {
            Biseccion rootFinder = new Biseccion(function, a, b, tolerance);
            double root = rootFinder.findRoot();

            // Mostrar resultado
            System.out.println("\nResultados para la función: " + customFunction);
            System.out.println("Raíz aproximada: " + root);
            System.out.println("Error es menor que: " + tolerance);

            // Verificar que el resultado esté dentro de la tolerancia
            double fRoot = function.applyAsDouble(root);
            if (Math.abs(fRoot) > tolerance) {
                System.out.println("\nAdvertencia: El valor de la función en la raíz es " + fRoot);
                System.out.println("Esto es mayor que la tolerancia especificada. Considere ajustar los parámetros.");
            }

            // Mostrar la tabla de iteraciones
            List<Biseccion.IterationData> iterations = rootFinder.getIterationHistory();

            if (!iterations.isEmpty()) {
                System.out.println("\nTabla de iteraciones:");
                System.out.println("+------+---------------+---------------+---------------+------------------------+--------------+");
                System.out.println("| Iter |       a       |       b       |       c       |         f(c)          |    Error     |");
                System.out.println("+------+---------------+---------------+---------------+------------------------+--------------+");

                for (Biseccion.IterationData data : iterations) {
                    System.out.printf("| %-4d | %-13.6f | %-13.6f | %-13.6f | %-22.12f | %-12.12f |\n",
                            data.getIteration(), data.getA(), data.getB(), data.getC(),
                            data.getFc(), data.getError());
                }

                System.out.println("+------+---------------+---------------+---------------+------------------------+--------------+");
            }

        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + (e.getMessage() != null ? e.getMessage() : "Parámetros inválidos para encontrar la raíz"));
            System.out.println("Por favor intente de nuevo con diferentes límites de intervalo o tolerancia.");
        } catch (Exception e) {
            System.err.println("Error inesperado: " + (e.getMessage() != null ? e.getMessage() : "Ocurrió un error desconocido"));
            e.printStackTrace();
            System.out.println("Por favor intente de nuevo con diferentes parámetros.");
        }
    }

    /**
     * Ejecuta el método del punto fijo.
     *
     * @param scanner El scanner para la entrada del usuario
     */
    private static void runFixedPointMethod(Scanner scanner) {
        System.out.println("\nMétodo de Punto Fijo");
        System.out.println("===================");

        // Ingresar una función personalizada
        System.out.println("\nIngrese la función g(x) en términos de x (o 'volver' para regresar al menú principal):");
        System.out.println("Esta debe ser la función iterativa, es decir, la funcion encontrada a partir de la funcion original f(x)");
        System.out.println("Ejemplos: (x^2 + 2)/3, cos(x), (x + 5/x)/2, sqrt(10-x^2)");
        System.out.println("Operaciones soportadas: +, -, *, /, ^");
        System.out.println("Funciones soportadas: sin, cos, tan, exp, log, sqrt");
        System.out.println("Constantes soportadas: e, pi");

        System.out.print("\nFunción g(x): ");
        String customFunction = scanner.nextLine();

        if (customFunction.trim().equalsIgnoreCase("volver")) {
            return;
        }

        if (customFunction.trim().isEmpty()) {
            System.out.println("La función no puede estar vacía. Por favor intente de nuevo.");
            return;
        }

        DoubleUnaryOperator function;
        try {
            function = PuntoFijo.parseFunction(customFunction);

            // Probar la función para asegurarse de que funciona, pero sin imprimir resultados
            function.applyAsDouble(0);
            function.applyAsDouble(1);
            function.applyAsDouble(2);
        } catch (Exception e) {
            System.out.println("Error al analizar la función: " + e.getMessage());
            System.out.println("Por favor intente de nuevo con una expresión diferente.");
            return;
        }

        // Obtener valor inicial
        double initialGuess;
        System.out.print("\nIngrese el valor inicial (x0): ");
        try {
            initialGuess = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida para el valor inicial. Por favor ingrese un número válido.");
            return;
        }

        // Obtener número máximo de iteraciones
        int maxIterations;
        System.out.print("Ingrese el número máximo de iteraciones: ");
        try {
            maxIterations = Integer.parseInt(scanner.nextLine());

            // Validar número de iteraciones
            if (maxIterations <= 0) {
                System.out.println("Error: El número de iteraciones debe ser positivo.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida para el número de iteraciones. Por favor ingrese un número entero válido.");
            return;
        }

        // Obtener tolerancia
        double tolerance;
        System.out.print("Ingrese la tolerancia de error (debe ser positiva): ");
        try {
            tolerance = Double.parseDouble(scanner.nextLine());

            // Validar tolerancia
            if (tolerance <= 0) {
                System.out.println("Error: La tolerancia debe ser positiva.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida para la tolerancia. Por favor ingrese un número válido.");
            return;
        }

        // Crear buscador de punto fijo y encontrar punto fijo
        try {
            PuntoFijo fixedPointFinder = new PuntoFijo(function, initialGuess, maxIterations, tolerance);
            double fixedPoint = fixedPointFinder.findFixedPoint();

            // Evaluar g(x) en el punto fijo encontrado
            double gOfFixedPoint = function.applyAsDouble(fixedPoint);

            // Calcular error final
            double finalError = Math.abs(gOfFixedPoint - fixedPoint);

            // Mostrar resultado
            System.out.println("\nResultados para la función g(x) = " + customFunction);
            System.out.println("Punto fijo aproximado: " + fixedPoint);
            System.out.println("g(" + fixedPoint + ") = " + gOfFixedPoint);
            System.out.println("Error final: " + finalError);

            // Verificar convergencia
            if (finalError > tolerance) {
                System.out.println("\nAdvertencia: El método no convergió dentro de la tolerancia especificada.");
                System.out.println("El error final es mayor que la tolerancia.");
                System.out.println("Puede intentar con más iteraciones o un valor inicial diferente.");
            }

            // Mostrar la tabla de iteraciones
            List<PuntoFijo.IterationData> iterations = fixedPointFinder.getIterationHistory();

            if (!iterations.isEmpty()) {
                System.out.println("\nTabla de iteraciones:");
                System.out.println("+------+------------------+------------------+------------------+");
                System.out.println("| Iter |        xn        |      g(xn)       |       Error      |");
                System.out.println("+------+------------------+------------------+------------------+");

                for (PuntoFijo.IterationData data : iterations) {
                    System.out.printf("| %-4d | %-16.10f | %-16.10f | %-16.10f |\n",
                            data.getIteration(), data.getXValue(), data.getGxValue(), data.getError());
                }

                System.out.println("+------+------------------+------------------+------------------+");
            }

        } catch (Exception e) {
            System.err.println("Error: " + (e.getMessage() != null ? e.getMessage() : "Ocurrió un error inesperado"));
            e.printStackTrace();
            System.out.println("Por favor intente de nuevo con diferentes parámetros.");
        }
    }
}