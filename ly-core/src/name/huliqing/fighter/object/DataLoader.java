/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.object;

import name.huliqing.fighter.data.Proto;
import name.huliqing.fighter.data.ProtoData;

/**
 * 载入数据接口
 * @author huliqing
 */
public interface DataLoader<T extends ProtoData> {
    
    void load(Proto proto, T store);
    
}
