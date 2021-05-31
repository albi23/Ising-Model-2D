import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

public class IsingModel2D {

    private static final Random rand = new Random(System.nanoTime());
    private static final String outMagnetisationFilePattern = "magnetization_file_L%d.txt";
    private static final String outHeatFilePattern = "heat_file_L%d.txt";
    private static final int MCS = 230_000;
    private static final int K0 = 30_000;

    private record Pair(double val1, double val2) {
    }

    public static void main(String[] args) {
        final IsingModel2D isingModel2D = new IsingModel2D();
        isingModel2D.avgHeatSimulation();
        isingModel2D.avgMagnetisationSimulation();
        isingModel2D.exampleSpinConfiguration();
    }

    public void avgMagnetisationSimulation() {
        int[] latticeSizeDef = {5, 10, 30,60};
        for (int l : latticeSizeDef)
            twoDimIsingModelSimulation(l, 1.5, 3.5, 0.01, outMagnetisationFilePattern, Pair::val1);
    }

    public void avgHeatSimulation() {
        int[] latticeSizeDef = {8, 16, 35};
        for (int l : latticeSizeDef)
            twoDimIsingModelSimulation(l, 1.0, 3.5, 0.01, outHeatFilePattern, Pair::val2);
    }

    private void periodicBoundaryConditionStep(int i, int j, int[][] matrix, double tStar) {
        int delta_energy = 2 * (matrix[i][j]) * getNeighborSum(i, j, matrix);
        if (delta_energy < 0 || rand.nextDouble() <= Math.exp(-delta_energy / tStar))
            matrix[i][j] = -matrix[i][j];
    }

    private int getNeighborSum(int i, int j, int[][] matrix) {
        int matrixSize = matrix.length;
        int up = (i == 0) ? matrix[matrixSize - 1][j] : matrix[i - 1][j];
        int down = (i == matrixSize - 1) ? matrix[0][j] : matrix[i + 1][j];
        int left = (j == 0) ? matrix[i][matrixSize - 1] : matrix[i][j - 1];
        int right = (j == matrixSize - 1) ? matrix[i][0] : matrix[i][j + 1];
        return (left + right + up + down);
    }

    private int[][] generateSquareMatrix(int size) {
        int[][] matrix = new int[size][size];
        IntStream.range(0, size)
                .forEach(i -> IntStream.range(0, size)
                        .forEach(j -> matrix[i][j] = rand.nextDouble() > 0.5 ? 1 : -1));
        return matrix;
    }


    private Pair MSCStep(int[][] matrix, double tStar) {
        int matrixSize = matrix.length;
        double avm = 0.0, avEnergy = 0.0, av2Energy = 0.0;
        double avgSteps = (MCS - K0) / 100.0;
        for (int k = 1; k <= MCS; k++) {
            for (int i = 0; i < matrixSize; i++)
                for (int j = 0; j < matrixSize; j++)
                    periodicBoundaryConditionStep(i, j, matrix, tStar);

            if (k > K0 && k % 100 == 0) {
                avm = avm + Math.abs(calculateMagnetisation(matrix, matrixSize));
                double energy = calculateEnergy(matrix, matrixSize);
                avEnergy += energy;
                av2Energy += (energy * energy);
            }
        }
        avm = avm / avgSteps;
        avEnergy /= avgSteps;
        av2Energy /= avgSteps;
        double heat = 1.0 / (matrixSize * matrixSize * tStar * tStar)
                * (av2Energy - (avEnergy * avEnergy));
        return new Pair(avm, heat);
    }

    private double calculateMagnetisation(int[][] matrix, int matrixSize) {
        double m = 0;  //magnetisation
        for (int[] ints : matrix)
            for (int j = 0; j < matrixSize; j++)
                m += ints[j];
        m = m / (matrixSize * matrixSize);
        return m;
    }

    private int calculateEnergy(int[][] matrix, int matrixSize) {
        int energy = 0;
        for (int i = 0; i < matrixSize; i++)
            for (int j = 0; j < matrixSize; j++)
                energy += (matrix[i][j] * getNeighborSum(i, j, matrix));
        return energy / 2;
    }


    private void twoDimIsingModelSimulation(int lattice_size, double t0, double t_end, double step,
                                            String outFilePattern,
                                            Function<Pair, Double> func) {

        t_end += 9.0E-13;
        try (var writer = new FileWriter(String.format(outFilePattern, lattice_size))) {
            for (double tStar = t0; tStar < t_end; tStar += step) {
                Pair data = MSCStep(generateSquareMatrix(lattice_size), tStar);
                writer.write(tStar + " " + func.apply(data) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exampleSpinConfiguration() {
        int[] latticeSizes = {8, 16, 35};
        double[] tStars = {1.0, 2.26, 10.0};
        for (int lSize : latticeSizes) {
            for (double tStar : tStars) {
                final int[][] matrix = generateSquareMatrix(lSize);
                for (int k = 1; k <= MCS; k++) {
                    for (int i = 0; i < lSize; i++)
                        for (int j = 0; j < lSize; j++)
                            periodicBoundaryConditionStep(i, j, matrix, tStar);
                }
                saveContent(matrix, String.format("config_L=%d_T=%f", lSize, tStar));
            }
        }
    }

    private static void saveContent(int[][] model, String outFilePattern) {
        final StringBuilder sb = new StringBuilder();
        for (int[] ints : model)
            sb.append(String.join(" ",
                    IntStream.of(ints)
                            .mapToObj(s -> "" + s)
                            .toArray(String[]::new))).append('\n');
        writeStringToFile(outFilePattern, sb.toString());
    }

    private static void writeStringToFile(final String outPath, final String content) {
        try (final FileWriter fileWriter = new FileWriter(outPath)) {
            fileWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int[][] arrayCopy(int[][] source) {
        final int[][] destination = new int[source.length][source.length];
        for (int i = 0; i < source.length; i++)
            System.arraycopy(source[i], 0, destination[i], 0, source[i].length);
        return destination;
    }
}
