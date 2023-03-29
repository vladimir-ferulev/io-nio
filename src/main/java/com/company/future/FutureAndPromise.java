package com.company.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

// todo переместить в проект с Future и Promise
// наглядный пример Promise в виде CompletableFuture
public class FutureAndPromise {
    public static void main(String[] args) {
        // Ты обманом заставляешь свою маму обещать (promise) тебе деньги, она дает тебе это
        // обещание (promise), но на самом деле она не торопится его выполнять
        Supplier<Integer> momsWallet = () -> {
            try {
                Thread.sleep(1000);     // мама занята
            } catch (InterruptedException ignored) { }

            return 100;
        };

        ExecutorService ex = Executors.newFixedThreadPool(10);
        CompletableFuture<Integer> promise = CompletableFuture.supplyAsync(momsWallet, ex);

        // Вы счастливы, вы хотите поблагодарить вашу маму сразу после того, как она даст деньги
        promise.thenAccept(money -> System.out.println("Thank you mom for $" + money));

        // Но твой отец вмешивается и срывает планы мамы и выполняет обещание (устанавливает ему значение)
        // с гораздо меньшим вкладом быстро и очень решительно, в то время как мама медленно открывает
        // свой кошелек (вспомните Thread.sleep(1000)):
        promise.complete(10);
    }
}
/*
  В результате будет выведено:
Thank you mom for $10

  Обещание мамы было создано, но дождалось только до какого-то события "завершения" (completion)
CompletableFuture<Integer> promise...

  Вы создали такое событие, приняв ее обещание и объявив о своих планах поблагодарить маму:
promise.thenAccept...

  В этот момент мама начинает открывать кошелек, но очень медленно и папа быстрее успевает
  выполнить обещание вместо мамы:
promise.complete(10);

ExecutorService был явно создан для того, чтобы поток не был демоном. И если бы не было преждевременного
завершения обещания
    promise.complete(10);
то программа не стала бы дожидаться выполнения асинхронной таски и завершилась бы.
*/