import java.util.ArrayList;
import java.util.Scanner;
import java.util.NoSuchElementException;

public class Menu {

	public static String lineSeperator = "-".repeat(32) + "\n";

	private String message;
	private String invalidInputMessage;
	private String inputPromptMessage;
	
	private int optionCount;
	
	private ArrayList<Runnable> menuOptions = new ArrayList<>();
	private Runnable onStart = () -> {return;};
	
	public Menu(String message, int optionCount) {
		this.message = message;
		this.optionCount = optionCount;
	
		this.inputPromptMessage = String.format("Please enter an option [1-%d]: ", optionCount);
		this.invalidInputMessage = "Invalid input. Please try again\n";

		// set a static function to throw an exception as default behaviour for all options. Before any are set
		for (int i = 0; i < optionCount; i++) this.menuOptions.add(Menu::unsetOption);
	}

	// this is where every menu starts
	public void start(Scanner scanner) throws RuntimeException {
	
		 // runs a function before the message prints. 
		 // There might be a use case to run function after message.
		 // Maybe add attribute to decide whether to print before or after the onStart function. Defaulting to before
		onStart.run();
		
		System.out.print(message); // prints out the main body of the menu. This can be literally anything and is defined in the Menus constructor

		String res; // user response
		int selectedOption; // parsed into integer to use as an index into an array

		while (true) {
			System.out.print(inputPromptMessage);
			//res = scanner.next();

			/*
			 first phase of input validation. If user doesn't input a valid integer then 
			 exception is thrown and user in prompted to enter again
			 */
			try { 
				//selectedOption = Integer.parseInt(res);
				selectedOption = scanner.nextInt();
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

				System.out.print(Menu.lineSeperator);

				// run selected option
				menuOptions.get(selectedOption-1).run();

				// pause until next input from user
				System.out.print("\nPress enter to continue...\n");
				scanner.nextLine();
				try {
					scanner.nextLine();
				} catch (NoSuchElementException e) {
					// The way scanner behaves seems inconsistant. 
					// After a process from the PCI devices is finished, scanner.nextLine() is required to run twice
					// For other processes it is required only once
					// Causes exception in some cases too...
				}

				System.out.print(Menu.lineSeperator);

				// re-run this menu
				this.start(scanner);
				
			} else {
				System.out.print(invalidInputMessage);
				continue;
			}

			break;
			
		}
	}

	// link a function to an option index
	public void setOption(int index, Runnable optionMethod) {
		try {
			menuOptions.set(index-1, optionMethod);
		} catch (IndexOutOfBoundsException e) {
			System.out.printf("Incorrect amount of options specified for this menu \n");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void setOnStart(Runnable onStart) {
		this.onStart = onStart;
	}

	// static default option function
	private static void unsetOption() throws RuntimeException {
		throw new  RuntimeException("Menu option not set");
	}

}
