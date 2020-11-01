package cinema;

import person.Person;

/**
 * @author Haim Adrian
 * @since 01-Nov-20
 */
public class Cinema {
    private final Person[][] seats;
    private final int rows;
    private final int cols;

    public Cinema(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        seats = new Person[rows][cols];
    }

    public boolean isSeatAvailable(int row, int col) throws IndexOutOfBoundsException {
        return seats[row][col] == null;
    }

    public Person[][] getSeats() {
        return seats;
    }

    public boolean orderSeat(int row, int col, Person p) {
        if (isSeatAvailable(row, col)) {
            synchronized (seats) {
                if (isSeatAvailable(row, col)) {
                    seats[row][col] = p;
                    return true;
                }
            }
        }

        return false;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    @Override
    public String toString() {
        StringBuilder cinemaString = new StringBuilder();

        for (int i = 0; i < rows; i++) {
            if (cinemaString.length() > 0) {
                cinemaString.append(System.lineSeparator());
            }

            for (int j = 0; j < cols; j++) {
                Person p = seats[i][j];
                cinemaString.append(String.format(" %-10s ", (p == null ? "-" : p.getName())));
            }
        }

        return cinemaString.toString();
    }
}

