package com.project;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.project.exceptions.SudokuAlreadySolved;
import com.project.exceptions.SudokuUnsolvable;

public class Sudoku {

  private Sudoku() {
  }

  public static void solve(Board board) throws SudokuAlreadySolved {
    Deque<Board> sudokusToCome = new LinkedList<>();
    if (fillOneBoard(board, sudokusToCome)) {
      throw new SudokuAlreadySolved();
    }
  }

  private static boolean fillOneBoard(Board board, Deque<Board> sudokusToCome) {
    ArrayList<Integer> tilesLogic;
    ArrayList<Boolean> tilesPossibilities;
    int size = board.getSize();
    tilesLogic = new ArrayList<>(size * size);
    tilesPossibilities = new ArrayList<>(size * size * size);
    for (int i = 0; i < size * size * size; ++i) {
      tilesPossibilities.add(true);
    }
    for (int i = 0; i < size * size; ++i) {
      tilesLogic.add(size);
    }
    while (true) {
      updateLogic(board, tilesLogic, tilesPossibilities);
      if (isSolvableInternal(board, tilesLogic)) {
        solveTick(board, tilesLogic, tilesPossibilities, sudokusToCome);
      } else {
        if (isSolved(board)) {
          return true;
        }
        if (!sudokusToCome.isEmpty()) {
          board = sudokusToCome.removeLast();
        } else {
          return false;
        }
      }
    }
  }

  private static Hint solveTick(Board board, ArrayList<Integer> tilesLogic, ArrayList<Boolean> tilesPossibilities,
      Deque<Board> sudokusToCome) {
    int currentIndex = findSmallest(board, tilesLogic);
    int x = currentIndex / board.getSize();
    int y = currentIndex - x * board.getSize();
    if (tilesLogic.get(currentIndex) == 1) {
      Hint temp = new Hint(x, y, fetchNumber(board, tilesPossibilities, x, y));
      board.setTileValue(x, y, temp.getValue());
      return temp;
    } else if (tilesLogic.get(currentIndex) > 1) {
      addSudokuOntoStack(board, tilesLogic, tilesPossibilities, sudokusToCome, x, y, currentIndex);
      return null;
    }
    throw new InvalidParameterException("Shouldn't be here");
  }

  private static void addSudokuOntoStack(Board board, ArrayList<Integer> tilesLogic,
      ArrayList<Boolean> tilesPossibilities, Deque<Board> sudokusToCome, int x, int y, int currentIndex) {
    int size = board.getSize();
    int doubleSize = size * size;
    int j = 0;
    for (int i = 1; i < tilesLogic.get(currentIndex); ++i) {
      for (; j < size; ++j) {
        if (Boolean.TRUE.equals(tilesPossibilities.get(x * doubleSize + y * size + j))) {
          Board tempBoard = board;
          tempBoard.setTileValue(x, y, j + 1);
          sudokusToCome.add(tempBoard);
          ++j;
          break;
        }
      }
    }
    for (; j < size; ++j) {
      if (Boolean.TRUE.equals(tilesPossibilities.get(x * doubleSize + y * size + j))) {
        board.setTileValue(x, y, j + 1);
        break;
      }
    }
  }

  private static boolean isSolved(Board board) {
    for (int i = 0; i < board.getSize(); ++i) {
      for (int j = 0; j < board.getSize(); ++j) {
        if (board.getTileValue(i, j) == 0) {
          return false;
        }
      }
    }
    return true;
  }

  private static boolean isSolvableInternal(Board board, ArrayList<Integer> tilesLogic) {
    boolean anyValue = false;
    for (int i = 0; i < board.getSize(); ++i) {
      for (int j = 0; j < board.getSize(); ++j) {
        boolean temp = board.getTileValue(i, j) == 0;
        if (temp && tilesLogic.get(i * board.getSize() + j) == 0) {
          return false;
        } else if (temp) {
          anyValue = true;
        }
      }
    }
    return anyValue;
  }

  private static void updateLogic(Board board, ArrayList<Integer> tilesLogic, ArrayList<Boolean> tilesPossibilities) {
    filledUpdate(board, tilesLogic, tilesPossibilities);
    for (int i = 0; i < board.getSize(); ++i) {
      for (int j = 0; j < board.getSize(); ++j) {
        rowLogic(board, tilesPossibilities, i, j);
        columnLogic(board, tilesPossibilities, i, j);
        boxLogic(board, tilesLogic, tilesPossibilities, i, j);
      }
    }
  }

  private static void filledUpdate(Board board, ArrayList<Integer> tilesLogic, ArrayList<Boolean> tilesPossibilities) {
    for (int i = 0; i < board.getSize(); ++i) {
      for (int j = 0; j < board.getSize(); ++j) {
        if (board.getTileValue(i, j) != 0) {
          int posInPossibilities = i * board.getSize() * board.getSize() + j * board.getSize();
          tilesLogic.set(i * board.getSize() + j, Integer.valueOf(0));
          for (int k = 0; k < board.getSize(); ++k) {
            tilesPossibilities.set(posInPossibilities + k, false);
          }
        }
      }
    }
  }

  private static void rowLogic(Board board, ArrayList<Boolean> tilesPossibilities, int x, int y) {
    // if it needs refreshing breeze through line
    if (board.getTileValue(x, y) == 0) {
      int posInPossibilities = x * board.getSize() * board.getSize() + y * board.getSize();
      for (int i = 0; i < board.getSize(); ++i) {
        if (board.getTileValue(i, y) != 0) {
          tilesPossibilities.set(posInPossibilities + board.getTileValue(i, y) - 1, false);
        }
      }
    }
  }

  private static void columnLogic(Board board, ArrayList<Boolean> tilesPossibilities, int x, int y) {
    if (board.getTileValue(x, y) == 0) {
      int posInPossibilities = x * board.getSize() * board.getSize() + y * board.getSize();
      for (int i = 0; i < board.getSize(); ++i) {
        if (board.getTileValue(x, i) != 0) {
          tilesPossibilities.set(posInPossibilities + board.getTileValue(x, i) - 1, false);
        }
      }
    }
  }

  private static void boxLogic(Board board, ArrayList<Integer> tilesLogic, ArrayList<Boolean> tilesPossibilities, int x,
      int y) {
    if (board.getTileValue(x, y) == 0) {
      int tempX;
      int tempY;
      int posInPossibilities = x * board.getSize() * board.getSize() + y * board.getSize();
      tempX = (x / board.getBoxWidth()) * board.getBoxWidth();
      tempY = (y / board.getBoxHeight()) * board.getBoxHeight();
      for (int i = tempX; i < tempX + board.getBoxWidth(); ++i) {
        for (int j = tempX; j < tempY + board.getBoxHeight(); ++j) {
          if (board.getTileValue(i, j) != 0) {
            tilesPossibilities.set(posInPossibilities + board.getTileValue(i, j) - 1, false);
          }
        }
      }
      int count = 0;
      for (int i = 0; i < board.getSize(); ++i) {
        if (Boolean.TRUE.equals(tilesPossibilities.get(posInPossibilities + i))) {
          ++count;
        }
      }
      tilesLogic.set(x * board.getSize() + y, Integer.valueOf(count));
    }
  }

  private static int findSmallest(Board board, ArrayList<Integer> tilesLogic) {
    int smallest = board.getSize() + 1;
    int index = -1;
    for (int i = 0; i < board.getSize(); ++i) {
      for (int j = 0; j < board.getSize(); ++j) {
        if (tilesLogic.get(i * board.getSize() + j) != 0 && tilesLogic.get(i * board.getSize() + j) < smallest) {
          smallest = tilesLogic.get(i * board.getSize() + j);
          index = i * board.getSize() + j;
        }
      }
    }
    return index;
  }

  private static int fetchNumber(Board board, ArrayList<Boolean> tilesPossibilities, int x, int y) {
    for (int i = 0; i < board.getSize(); ++i) {
      Boolean condition = tilesPossibilities.get(x * board.getSize() * board.getSize() + y * board.getSize() + i);
      if (Boolean.TRUE.equals(condition)) {
        return i + 1;
      }
    }
    return 0;
  }

  public static Hint hint(Board in) throws SudokuAlreadySolved, SudokuUnsolvable {
    Deque<Board> sudokusToCome = new LinkedList<>();
    ArrayList<Integer> tilesLogic;
    ArrayList<Boolean> tilesPossibilities;
    Board board = in;
    int size = board.getSize();
    tilesLogic = new ArrayList<>(size * size);
    tilesPossibilities = new ArrayList<>(size * size * size);
    for (int i = 0; i < size * size * size; ++i) {
      tilesPossibilities.add(true);
    }
    for (int i = 0; i < size * size; ++i) {
      tilesLogic.add(size);
    }
    while (true) {
      updateLogic(board, tilesLogic, tilesPossibilities);
      if (isSolvableInternal(board, tilesLogic)) {
        return solveTick(board, tilesLogic, tilesPossibilities, sudokusToCome);
      } else {
        if (isSolved(board)) {
          throw new SudokuAlreadySolved();
        }
        if (!sudokusToCome.isEmpty()) {
          board = sudokusToCome.removeLast();
        } else {
          throw new SudokuUnsolvable();
        }
      }
    }
  }

  private static ArrayList<Hint> compareSudokus(Board main, Board solved) {
    ArrayList<Hint> returnValue = new ArrayList<>();
    for (int i = 0; i < main.getSize() * main.getSize(); ++i) {
      int x = i / main.getSize();
      int y = i % main.getSize();
      if (main.getTileValue(x, y) != 0 && main.getTileValue(x, y) != solved.getTileValue(x, y)) {
        returnValue.add(new Hint(x, y, solved.getTileValue(x, y)));
      }
    }
    return returnValue;
  }

  public static Boolean isSolvable(Board board) {
    Deque<Board> sudokusToCome = new LinkedList<>();
    Board temp = board;
    return fillOneBoard(temp, sudokusToCome);
  }

  // this method checks if this Board has an solution
  // if even one solution exists, will return true
  public static List<Hint> check(Board in, Board original) throws SudokuUnsolvable {
    Deque<Board> sudokusToCome = new LinkedList<>();
    Deque<Board> correctSudokus = new LinkedList<>();
    Board board = original;
    if (!fillOneBoard(board, sudokusToCome)) {
      throw new SudokuUnsolvable();
    }
    correctSudokus.push(board);
    Board temp = sudokusToCome.removeLast();
    while (fillOneBoard(temp, sudokusToCome)) {
      correctSudokus.push(temp);
      temp = sudokusToCome.removeLast();
    }
    ArrayList<Hint> min = new ArrayList<>();
    for (int i = 0; i < in.getSize() * in.getSize(); ++i) {
      min.add(new Hint(0, 0, 0));
    }
    for (Board one : correctSudokus) {
      ArrayList<Hint> data = compareSudokus(in, one);
      if (data.size() <= min.size()) {
        min = data;
      }
    }
    return min;
  }
}
