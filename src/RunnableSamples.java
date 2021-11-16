import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class RunnableSamples {
    static User user;
    static Incremenator mInc;

    static int value = 0;
    static int mValue = 0;


    //асинхронное выполнение потоков
    static void taskOne() {
        user = new User();
        Thread userThread = new Thread(user);
        userThread.start();

        System.out.println("Hello from main thread");
    }

    //ожидание выполнения другого потока
    static void taskTwo() {
        Runnable battle = () -> {
            System.out.println(1);
            for (int i = 0; i < 5; i++) {
                try {
                    sleep(1000);        //Приостанавливает поток на 1 секунду
                } catch (InterruptedException e) {
                    System.out.println("Interrupt | Прерван");
                }

                System.out.println("Java тормозит!");
            }
        };
        System.out.println("Battle begins....");
        Thread battleThread = new Thread(battle);
        battleThread.start();
        for (int i = 0; i < 5; i++) {
            try {
                sleep(1000);        //Приостанавливает main поток на 1 секунду
            } catch (InterruptedException e) {
                System.out.println("Interrupt | Прерван");
            }

            System.out.println("Java not тормозит!");
        }

        if (battleThread.isAlive()) {
            try {
                battleThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("In the end. Java тормозит!");
        } else {
            System.out.println("In the end. Java not тормозит!");
        }

        System.out.println("Battle stopped!");
    }

    //управление потоком из другого потока: изменение значения переменной в потоке
    static void taskThree() {
        mInc = new Incremenator();    //Создание потока

        System.out.print("Значение = ");

        mInc.start();    //Запуск потока

        //Троекратное изменение действия инкременатора
        //с интервалом в i*2 секунд
        for (int i = 1; i <= 3; i++) {
            try {
                sleep(i * 2 * 1000); //Ожидание в течении i*2 сек.
            } catch (InterruptedException e) {
                System.out.println("Interrupt!");
            }

            mInc.changeAction();    //Переключение действия
        }

        mInc.interrupt();    //Инициация завершения побочного потока
    }

    //синхронизация потоков по локу
    static void taskFour() {
        Object lock = new Object();

        Runnable task = () -> {
            System.out.println(1);
            synchronized (lock) {
                System.out.println("thread");
                System.out.println(2);
            }
        };

        Thread th1 = new Thread(task);
        th1.start();
        synchronized (lock) {
            for (int i = 0; i < 8; i++) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.print("  " + i);
            }
            System.out.println(" ...");
        }
    }

    //wait and notify оповещение по локу
    static void taskFive() {
        Object lock = new Object();
        // task будет ждать, пока его не оповестят через lock
        Runnable task = () -> {
            synchronized (lock) {
                try {
                    System.out.println(3);
                    lock.wait();
                    System.out.println(4);

                } catch (InterruptedException e) {
                    System.out.println("interrupted");
                }
            }
            // После оповещения нас мы будем ждать, пока сможем взять лок
            System.out.println("thread");
        };
        Thread taskThread = new Thread(task);
        taskThread.start();
        // Ждём и после этого забираем себе лок, оповещаем и отдаём лок
        try {
            sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("main");
        System.out.println(1);
        synchronized (lock) {
            lock.notify();
            System.out.println(2);
        }
    }

    //park and unpark
    static void taskSix() {
        Runnable task = () -> {
            System.out.println(2);
            //Запаркуем текущий поток
            System.err.println("Will be Parked");
            LockSupport.park();

            // Как только нас распаркуют - начнём действовать
            System.err.println("Unparked");
            System.out.println(2 + 2);
        };
        System.out.println(1);
        Thread th = new Thread(task);
        th.start();
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.err.println("Thread state: " + th.getState());
        System.out.println(1 + 2);
        LockSupport.unpark(th);
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //lock and unlock
    static void taskSeven() {
        Lock lock = new ReentrantLock();
        Runnable task = () -> {
            System.out.println(3);
            lock.lock();
            System.out.println("Thread");
            System.out.println(6);
//            lock.unlock();
            System.out.println(4);
        };
        System.out.println(1);
        lock.lock();

        Thread th = new Thread(task);
        th.start();
        System.out.println("main");
        try {
            sleep(2000);
            System.out.println(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lock.unlock();
        System.out.println(5);
    }

    //DeadLock
    static void taskEight() {
        final Friend alphonse = new Friend("Alphonse");
        final Friend gaston = new Friend("Gaston");
        new Thread(() -> alphonse.bow(gaston)).start();
        new Thread(() -> gaston.bow(alphonse)).start();
    }

    //Race Condition - состояние гонки if without synchronized
    static void taskNine() {
        Object lock = new Object();
        Runnable task = () -> {
            synchronized (lock) {
                for (int i = 0; i < 10000; i++) {
                    int oldValue = value;
                    int newValue = ++value;
                    if (oldValue + 1 != newValue) {
                        throw new IllegalStateException(oldValue + " + 1 = " + newValue);
                    }
                }
                System.out.println(value);
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(task).start();
        new Thread(task).start();
        new Thread(task).start();
    }

    public static void main(String[] args) {
        taskNine();
    }
}

class User implements Runnable {

    @Override
    public void run() {
        System.out.println("Hello from User.cLass thread");
    }
}

class Incremenator extends Thread {
    private volatile boolean mIsIncrement = true; //volatile - используется для переменных, которые используются разными потоками

    public void changeAction()    //Меняет действие на противоположное
    {
        mIsIncrement = !mIsIncrement;
    }

    @Override
    public void run() {
        do {
            if (!Thread.interrupted())    //Проверка на необходимость завершения
            {
                if (mIsIncrement)
                    RunnableSamples.mValue++;    //Инкремент
                else
                    RunnableSamples.mValue--;    //Декремент

                //Вывод текущего значения переменной
                System.out.print(RunnableSamples.mValue + " ");
            } else {
                return;        //Завершение потока
            }

            try {
                sleep(1000);        //Приостановка потока на 1 сек.
            } catch (InterruptedException e) {
                System.out.println("Interrupt!");
                return;
            }
        }
        while (true);
    }
}

class Friend {
    private final String name;

    public Friend(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public synchronized void bow(Friend bower) {
        System.out.format("%s: %s has bowed to me!%n",
                this.name, bower.getName());
        bower.bowBack(this);
    }

    public synchronized void bowBack(Friend bower) {
        System.out.format("%s: %s has bowed back to me!%n",
                this.name, bower.getName());
    }
}