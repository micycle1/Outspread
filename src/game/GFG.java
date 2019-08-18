package game;

import java.util.Stack;
/**
 * https://www.geeksforgeeks.org/maximum-size-rectangle-binary-sub-matrix-1s/
 * Player option: game will only give rects that can fit, or rects of any size, player must skip turn
 */
class GFG {

	private static int maxHist(int R, int C, byte row[]) {
		final Stack<Integer> result = new Stack<Integer>();
		int top_val;
		int max_area = 0;
		int area = 0;

		int i = 0;
		while (i < C) {
			if (result.empty() || row[result.peek()] <= row[i]) {
				result.push(i++);
			} else {
				top_val = row[result.peek()];
				result.pop();
				if (!result.empty()) {
					area = top_val * (i - result.peek() - 1);
				}
				max_area = Math.max(area, max_area);
			}
		}

		while (!result.empty()) {
			top_val = row[result.peek()];
			result.pop();
			area = top_val * i;
			if (!result.empty()) {
				area = top_val * (i - result.peek() - 1);
			}
			max_area = Math.max(area, max_area);
		}
		return max_area;
	}

	// Returns area of the largest rectangle with all 1s in
	// A[][]
	public static int maxRectangle(int R, int C, byte A[][]) {
		// Calculate area for first row and initialize it as
		// result
		int result = maxHist(R, C, A[0]);

		for (int i = 1; i < R; i++) {
			for (int j = 0; j < C; j++) {
				// if A[i][j] is 1 then add A[i -1][j]
				if (A[i][j] == 1) { // 0 is free area
					A[i][j] += A[i - 1][j];
					result = Math.max(result, maxHist(R, C, A[i]));
					System.out.println(result);
				}
			}
		}
		return result;
	}
	
	public static void maxDimensions(byte[][] array) {
		
	}

	// Driver code
	public static void main(String[] args) {
		final byte A[][] = {
				{0, 0, 0, 1}, 
				{0, 0, 0, 1},
				{1, 1, 1, 1}, 
				{1, 1, 0, 1}};
		final int R = A.length; // y size (# of rows / size of column)
		final int C = A[0].length; // x size (# of columns / size of row)
		System.out.print("Area of maximum rectangle is " + maxRectangle(R, C, A));
	}
}