package name.huliqing.fighter.object;
 
import com.jme3.util.SafeArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 代码参考自：com.jme3.app.state.AppStateManager.
 * 精简单了一些方法
 * 主要用来管理自定义的Logic,
 */
public final class PlayManager<T extends PlayObject> {
    
    // 待初始化的状态列表
    private final SafeArrayList<T> initializing;
    
    // 运行时状态列表
    private final SafeArrayList<T> states;
    
    // 待清理的列表。
    private final SafeArrayList<T> terminating;
 
    public PlayManager(Class<T> type){
        initializing = new SafeArrayList<T>(type);
        states = new SafeArrayList<T>(type);
        terminating = new SafeArrayList<T>(type);
    }

    public boolean attach(T state){
        synchronized (states){
            if (!states.contains(state) && !initializing.contains(state)){
                initializing.add(state);
                return true;
            }else{
                return false;
            }
        }
    }

    public boolean detach(T state){
        synchronized (states){
            if (states.contains(state)){
//                state.onRemove();
                states.remove(state);
                terminating.add(state);
                return true;
            } else if(initializing.contains(state)){
//                state.onRemove();
                initializing.remove(state);
                return true;
            }else{
                return false;
            }
        }
    }

    public boolean hasState(T state){
        return states.contains(state) || initializing.contains(state);
    }
    
    /**
     * Calls update for attached states, do not call directly.
     * @param tpf Time per frame.
     */
    public void update(float tpf){
    
        // Cleanup any states pending
        terminatePending();

        // Initialize any states pending
        initializePending();

        // Update enabled states    
        T[] array = getStates();
        for (T state : array){
            state.update(tpf);
        }
    }

    public void cleanup(){
        for (T state : states.getArray()){
            state.cleanup();
        }
        states.clear();
        for (T state : terminating.getArray()){
            state.cleanup();
        }
        terminating.clear();
        initializing.clear();
    }

    // 不需要这个方法
//    /**
//     * Returns the first state that is an instance of subclass of the specified class.
//     * @param <T>
//     * @param stateClass
//     * @return First attached state that is an instance of stateClass
//     */
//    public <T extends T> T getState(Class<T> stateClass){
////        T[] array = getStates();
//        for (T state : states.getArray()) {
//            if (stateClass.isAssignableFrom(state.getClass())){
//                return (T) state;
//            }
//        }
//
//        // This may be more trouble than its worth but I think
//        // it's necessary for proper decoupling of states and provides
//        // similar behavior to before where a state could be looked
//        // up even if it wasn't initialized. -pspeed
////        array = getInitializing();
//        for (T state : initializing) {
//            if (stateClass.isAssignableFrom(state.getClass())){
//                return (T) state;
//            }
//        }
//        
//        return null;
//    }

    private void initializePending(){
        T[] array = getInitializing();
        if (array.length == 0)
            return;

        synchronized( states ) {
            // Move the states that will be initialized
            // into the active array.  In all but one case the
            // order doesn't matter but if we do this here then
            // a state can detach itself in initialize().  If we
            // did it after then it couldn't.
            List<T> transfer = Arrays.asList(array);         
            states.addAll(transfer);
            initializing.removeAll(transfer);
        }
        for (T state : array) {
            
            // 避免重复实始化
            if (state.isInitialized()) {
                continue;
            }
            
            state.initialize();
        }
    }
    
    private void terminatePending(){
        T[] array = getTerminating();
        if (array.length == 0)
            return;

        for (T state : array) {
            state.cleanup();
        }
        
        synchronized( states ) {
            // Remove just the states that were terminated...
            // which might now be a subset of the total terminating
            // list.
            terminating.removeAll(Arrays.asList(array));         
        }
    }    
    
    /**
     * 获取待初始化的列表（这些对象“还未”进行过initialize()）,即还未进行过
     * 初始化，正在等待初始化中.
     * @return 
     */
    public final T[] getInitializing() { 
        synchronized (states){ // 注意：这里同步的是运行时队列
            return initializing.getArray();
        }
    } 
    
    /**
     * 获取正在执行中的对象列表,(已经调用过initialize()方法),即已经进行过初
     * 始化的对象列表。
     * @return 
     */
    public final T[] getStates(){
        synchronized (states){ // 注意：这里同步的是运行时队列
            return states.getArray();
        }
    }

    /**
     * 获取等待清理的对象列表，这些对象已经从运行时列表（states）中移出，
     * 但还没有经过cleanup的对象。
     * @return 
     */
    public final T[] getTerminating() { 
        synchronized (states){ // 注意：这里同步的是运行时队列
            return terminating.getArray();
        }
    } 
}
