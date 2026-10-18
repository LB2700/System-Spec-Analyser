import java.util.ArrayList;
import java.util.Scanner;

public class Menu {

	private String message;
	private String invalidInputMessage;
	private String inputPromptMessage;
	
	private int optionCount;
	
	private ArrayList<Runnable> menuOptions = new ArrayList<>();
	
	public Menu(String message, int optionCount) {
		this.message = message;
		this.optionCount = optionCount;
	
		this.inputPromptMessage = String.format("Please enter an option [1-%d]: ", optionCount);
		this.invalidInputMessage = "Invalid input. Please try again\n";

		// set a static function to throw an exception as default behaviour for all options. Before any are set
		for (int i = 0; i < optionCount; i++) this.menuOptions.add(Menu::unsetOption);
	}

	// where every menu actually starts
	public void starts(Scanner scanner) throws RuntimeException {
		System.out.print(message); // prints out the main body of the menu. This can be literally anything and is defined in the Menus constructor

		String res; // user response
		int selectedOption; // parsed into integer to use as an index into an array

		while (true) {
			System.out.print(inputPromptMessage);
			res = scanner.next();

			/*
			 first phase of input validation. If user doesn't input a valid integer then 
			 exception is thrown and user in prompted to enter again
			 */
			try { 
				selectedOption = Integer.parseInt(res);
			} catch (NumberFormatException e) {
				System.out.print(invalidInputMessage);
				continue;
			}

			/*
			second phase of input validation. If inputted option isn't within the [1-N] range
			specfied in the prompt, input validaiton will fail and user will be prompted again
			*/
			if (selectedOption > 0 && selectedOption <= optionCount) { 
				/*
				run the function linked to the selected option index. Not garunteed to be set. If 
				an unset function is chosen by the user then the program will crash. 
				 */
				menuOptions.get(selectedOption-1).run();
			} else {
				System.out.print(invalidInputMessage);
				continue;
			}

			break;
			
		}
	}

	// link a function to an option index
	public void setOption(int index, Runnable optionMethod) {
		menuOptions.set(index-1, optionMethod);
	}

	// static default option function
	private static void unsetOption() throws RuntimeException {
		throw new  RuntimeException("Menu option not set");
	}

}
