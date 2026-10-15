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
		
		for (int i = 0; i < optionCount; i++) this.menuOptions.add(Menu::unsetOption);
	}

	public void run(Scanner scanner) throws RuntimeException {
		System.out.print(message);

		String res;
		int selectedOption;

		while (true) {
			System.out.print(inputPromptMessage);
			res = scanner.next();

			try {
				selectedOption = Integer.parseInt(res);
			} catch (NumberFormatException e) {
				System.out.print(invalidInputMessage);
				continue;
			}

			if (selectedOption > 0 && selectedOption <= optionCount) {
				menuOptions.get(selectedOption-1).run();
			} else {
				System.out.print(invalidInputMessage);
				continue;
			}

			break;
			
		}
	}

	public void setOption(int index, Runnable optionMethod) {
		menuOptions.set(index-1, optionMethod);
	}

	private static void unsetOption() throws RuntimeException {
		throw new  RuntimeException("Menu option not set");
	}

}
