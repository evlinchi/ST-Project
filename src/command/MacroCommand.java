package command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Съставна команда: няколко команди, които се изпълняват и отменят като едно действие.
 * Ползва се за замяна на селекция = изтриване + добавяне. В историята се вижда като един ред,
 * и едно натискане на Undo отменя и двете.
 */
public class MacroCommand implements Command {

    private final String description;
    private final List<Command> commands;

    public MacroCommand(String description, Command... commands) {
        this.description = description;
        this.commands = Collections.unmodifiableList(Arrays.asList(commands));
    }

    @Override
    public void execute() {
        int done = 0;
        try {
            for (Command command : commands) {
                command.execute();
                done++;
            }
        } catch (RuntimeException e) {
            // Ако някоя стъпка се провали, връщаме вече изпълнените, за да не остане документът наполовина променен.
            for (int i = done - 1; i >= 0; i--) {
                commands.get(i).undo();
            }
            throw e;
        }
    }

    /** Отменя командите в обратен ред: последната изпълнена се отменя първа. */
    @Override
    public void undo() {
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }

    @Override
    public String getDescription() {
        return description;
    }
}
