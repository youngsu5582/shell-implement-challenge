## State Machine

CS 에서 시스템 동작을 모델링하기 위해 사용하는 개념
시스템이 가질 수 있는 상태 (State) 와 상태들 간의 전이 (Transition) 을 정의

시스템이 특정 시점에 어떤 상태에 있으며 이벤트, 조건에 따른 상태로 전환 가능한 걸 설명

![](https://i.imgur.com/gHj5bsr.png)

### 상태 머신을 사용하는 이유

처음에는 절차 지향적 프로그래밍에서  
`if-else` 구문과 `switch-case` 구문과 크게 다르지 않아 봉리 수 있다.  

=> 키는 '안정성'에 있다!  

> 설계 자체에 결함이 없더라도, 소프트웨어 버그 발생 위험 가능성이 존재한다.
> (변수 변경, 잘못된 플로우로 전이)
> 멀티프로세싱, 멀티 프로세스 환경은 변수를 오염 시킬수 있다. - 여러 스레드가 공유
> -> 더 많은 버그 유발 가능
> '상태가 사전에 정의되어 있어 결함이 발생할 가능성이 낮아진다' 고 한다.

### 기본요소

- 상태 : 시스템이 존재할 수 있는 모든 조건이나 상황
- 전이 : 한 상태에서 다른 상태로의 변화, 특정 이벤트 & 조건이 충족될 때 발생
- 이벤트 : 상태 전이 유발하는 내,외부 발생 - EX) 사용자 입력, 시간 경과 등등
- 액션 : 상태 전이와 함께 실행되는 활동이나 작업 - 상태 변경하거나 외부 시스템에 신호 보낼때 사용

### 예시

실생활의 음료 자판기라고 가정해보면?

> 상태, 전이 그리고 액션을 잘 생각해보자.

- 상태

- 대기 상태 : 사용자의 입력을 기다리는 상태 - 돈을 투입 or 버튼을 누르는 걸 기다림
- 돈 투입 상태 : 사용자가 돈을 투입한 상태 - 돈을 더 넣거나 or 음료 선택을 기다림
- 금액 부족 상태 : 사용자가 선택한 음료수보다 돈이 부족한 상태 - 돈을 더 넣어달라고 안내
- 음료 선택 상태 : 사용자가 음료를 선택한 상태 - 음료를 제공하고, 잔돈 반환
- 음료 제공 & 잔돈 반환 상태 : 음료, 잔돈을 사용자에게 제공한 상태 - 자판기는 다시 대기 상태 이동

- 이벤트

- 돈 투입 이벤트 : 사용자가 자판기에 돈 투입하는 행위, `대기 상태 -> 돈 투입 상태` 로 전환
- 음료 선택 이벤트 : 사용자가 특정 음료 선택하는 행위 , `돈 투입 상태` 에서 충분한 금액이 투입되면 `음료 선택 상태` 로 전환
- 추가 금액 투입 이벤트 : 사용자가 금액 부족 상태에서 추가 돈 투입하는 행위, `금액 부족 상태` 에서 `돈 투입 상태` 로 돌아가거나, `음료 상태` 로 전환
- 제품 배출 완료 이벤트 : 선택된 음료를 사용자에게 제공하는 행위, `음료 선택 상태` 에서 `음료 제공 및 잔돈 반환상태` 로 전환
- 잔돈 반환 완료 이벤트 : 잔돈이 있는 경우, 잔돈을 반환하는 행위, `음료 제공 및 잔돈 반환 상태` 에서 `대시 상태` 로 전환
- 취소 이벤트 : 사용자가 선택 과정을 취소하는 행위, `어느 상태` 에서든 발생 가능, `초기 대기 상태` 로 전환

![](https://i.imgur.com/mFKPdx6.png)

이거 꽤나 복잡한거 같긴 하다... 좋은건가?

---

- 게임 개발
- 프론트엔드 : 
- 네트워크 프로토콜 : 3-way handshake

```java
// State.java
public interface State {
    void insertCoin(int amount);      // 돈 넣기
    void selectBeverage();            // 음료 선택
    void dispense();                  // 음료 배출
}
```

```java
// VendingMachine.java
public class VendingMachine {
    // 상태 객체들을 미리 생성해둠
    State waitingState;
    State hasMoneyState;
    State soldState;

    // 현재 상태를 담는 변수
    State state;
    
    int balance = 0; // 현재 잔액

    public VendingMachine() {
        waitingState = new WaitingState(this);
        hasMoneyState = new HasMoneyState(this);
        soldState = new SoldState(this);

        // 초기 상태는 '대기'
        this.state = waitingState;
    }

    // 상태 변경 메서드
    public void setState(State state) {
        this.state = state;
    }

    // 행동 위임 (Delegation) - 여기가 핵심!
    public void insertCoin(int amount) {
        state.insertCoin(amount);
    }
    
    public void selectBeverage() {
        state.selectBeverage();
        state.dispense(); // 선택 후 배출 시도 (내부 로직에 따라 결정)
    }

    // Getter & Setter for logic
    public State getWaitingState() { return waitingState; }
    public State getHasMoneyState() { return hasMoneyState; }
    public State getSoldState() { return soldState; }
    
    public void addBalance(int amount) { this.balance += amount; }
    public int getBalance() { return balance; }
    public void deductBalance(int price) { this.balance -= price; }
}
```

```java
public class HasMoneyState implements State {
    VendingMachine machine;

    public HasMoneyState(VendingMachine machine) {
        this.machine = machine;
    }

    @Override
    public void insertCoin(int amount) {
        System.out.println(amount + "원을 추가로 넣었습니다.");
        machine.addBalance(amount);
        System.out.println("현재 잔액: " + machine.getBalance());
        // 상태 유지 (계속 돈 넣기 가능)
    }

    @Override
    public void selectBeverage() {
        System.out.println("음료를 선택했습니다.");
        // 상태 전이: 돈 투입 -> 음료 배출 중
        machine.setState(machine.getSoldState());
    }

    @Override
    public void dispense() {
        System.out.println("음료를 선택해야 나옵니다.");
    }
}
```

```java
public class SoldState implements State {
    VendingMachine machine;

    public SoldState(VendingMachine machine) {
        this.machine = machine;
    }

    @Override
    public void insertCoin(int amount) {
        System.out.println("잠시만 기다려주세요. 음료 배출 중입니다.");
    }

    @Override
    public void selectBeverage() {
        System.out.println("이미 음료가 나오고 있습니다.");
    }

    @Override
    public void dispense() {
        machine.deductBalance(1000); // 음료 가격 차감 가정
        System.out.println("음료가 나왔습니다! (잔액: " + machine.getBalance() + ")");
        
        if (machine.getBalance() > 0) {
            System.out.println("잔돈 반환 중... 대기 상태로 복귀");
        } else {
            System.out.println("대기 상태로 복귀");
        }
        // 상태 전이: 배출 완료 -> 대기 상태
        machine.setState(machine.getWaitingState());
    }
}
```

-> if문을 사용하지 않을 수 있다.

플로우를 그림으로 그리고, 관리가 된다면 좋을거 같은 패턴