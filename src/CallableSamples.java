import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;

public class CallableSamples {

    //Work with callable task
    static void taskOne() {
        Callable<String> task = () -> "Hello, World!";

        FutureTask<String> future = new FutureTask<>(task);

        new Thread(future).start();

        System.out.println(1);
        try {
            sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            System.out.println(future.get()); // вызов синхронного метода get() (park механизм)
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    //Work with Functional interfaces
    static void taskTwo() throws InterruptedException {
        Supplier<String> supplier = new Supplier<String>() { //Поставщик. Он не имеет параметров, но возвращает что-то, то есть поставляет это.
            @Override
            public String get() {
                return "Supplier string";
            }
        };
        Consumer<String> consumer = new Consumer<String>() { //Потребитель. Он принимает на вход что-то (параметр s) и с этим что-то что-то делает, то есть потребляет что-то.
            @Override
            public void accept(String s) {
                System.out.println(s);
            }
        };
        Function<String, Integer> converter = new Function<String, Integer>() { //Функция. Она принимает на вход что-то (параметр s), что-то делает и возвращает что-то.
            @Override
            public Integer apply(String s) {
                return Integer.valueOf(s);
            }
        };

        System.out.println(supplier.get());
        sleep(7000);
        consumer.accept("Consumer string");
        sleep(7000);
        System.out.println(converter.apply("123"));
        sleep(7000);
    }

    public static void main(String[] args) {

        taskOne();

    }
}
