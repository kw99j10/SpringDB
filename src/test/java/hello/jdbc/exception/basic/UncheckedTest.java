package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class UncheckedTest {

    @Test
    void unchecked_catch(){
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void unchecked_throw(){
        Service service = new Service();
        Assertions.assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyUnCheckedException.class);
    }

    /**
     * RuntimeException을 상속 받은 예외는 언체크 예외가 된다.
     */
    static class MyUnCheckedException extends RuntimeException {
        public MyUnCheckedException(String message){
            super(message);
        }
    }

    /**
     * UnChecked 예외는
     * 예외를 잡거나, 던지지 않아도 된다.
     * 예외를 잡지 않으면 자동으로 밖으로 던짐
     */
    static class Service{
        Repository repository = new Repository();

        /**
         * 필요한 경우 예외를 잡아서 처리
         */
        public void callCatch(){
            try{
                repository.call();
            }
            catch (MyUnCheckedException e){
                log.info("예외 처리, message={}", e.getMessage(), e);
            }
        }

        /**
         * 예외를 잡지 않아도 자연스럽게 상위로 넘어감
         * throws 예외 선언 생략 가능
         */
        public void callThrow() {
            repository.call();
        }
    }


    static class Repository {
        public void call() {
            throw new MyUnCheckedException("ex");
        }
    }
}
