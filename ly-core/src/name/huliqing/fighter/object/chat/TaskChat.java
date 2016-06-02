/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.object.chat;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import name.huliqing.fighter.Factory;
import name.huliqing.fighter.constants.ResConstants;
import name.huliqing.fighter.data.ChatData;
import name.huliqing.fighter.enums.MessageType;
import name.huliqing.fighter.game.network.UserCommandNetwork;
import name.huliqing.fighter.game.service.ActorService;
import name.huliqing.fighter.game.service.PlayService;
import name.huliqing.fighter.game.service.TaskService;
import name.huliqing.fighter.game.view.tiles.ButtonPanel;
import name.huliqing.fighter.manager.ResourceManager;
import name.huliqing.fighter.manager.talk.Talk;
import name.huliqing.fighter.manager.talk.TalkImpl;
import name.huliqing.fighter.manager.talk.TalkListener;
import name.huliqing.fighter.object.actor.Actor;
import name.huliqing.fighter.object.task.Task;
import name.huliqing.fighter.ui.Text;
import name.huliqing.fighter.ui.UI;
import name.huliqing.fighter.ui.UIFactory;
import name.huliqing.fighter.ui.Window;

/**
 * 任务对话，用于玩家向NPC接任务或提交任务时的对话
 * @author huliqing
 */
public class TaskChat extends Chat {
    private final static PlayService playService = Factory.get(PlayService.class);
    private final static TaskService taskService = Factory.get(TaskService.class);
    private final static ActorService actorService = Factory.get(ActorService.class);
    private final static UserCommandNetwork userCommandNetwork = Factory.get(UserCommandNetwork.class);

    // 任务角色的类型
    private enum Role {
        /** 任务的发起者和结束者 */
        both, 
        /** 任务的发起者 */
        start,
        /** 任务的结束者 */
        end;
        public static Role identify(String name) {
            for (Role r : values()) {
                if (r.name().equals(name)) {
                    return r;
                }
            }
            throw new UnsupportedOperationException("Unknow role name, name=" + name);
        }
    }
    
    private String taskId;
    private Role role;
    
    // ---- inner
    private TaskRequestPanel requestPanel;
    private Actor player;
    private Task task;
    
    public TaskChat(ChatData data) {
        super(data);
        this.taskId = data.getAttribute("task");
        this.role = Role.identify(data.getAttribute("role", Role.both.name()));
    }

    @Override
    public void initialize() {
        super.initialize();
        // fix bug:如果requestPanel已经存在，即可能已经接过任务，则先移除，
        // 避免在接过任务后，在再次对话的时候仍然看到requestPanel
        if (requestPanel != null) {
            requestPanel.removeFromParent();
        }
        
        player = playService.getPlayer();
        task = taskService.getTask(player, taskId);
        
        // 玩家未接受过任务
        if (task == null) {
            task = taskService.loadTask(taskId);
            if (role == Role.start || role == Role.both) {
                Talk talk = new TalkImpl();
                for (String s : ResourceManager.getTaskChatStart(taskId)) {
                    talk.face(actor, player, false);
                    talk.speak(actor, s);
                }
                talk.addListener(new TalkListener() {
                    @Override
                    public void onTalkEnd() {
                        createRequestPanel(task);
                        root.attachChild(requestPanel);
                    }
                });
                actorService.talk(talk);
            }
            return;
        }
        
        // 玩家接过任务,并且任务已经完成过,则提示已经做过
        if (task.getData().isCompletion()) {
            playService.addMessage(ResourceManager.get(ResConstants.TASK_COMPLETED), MessageType.notice);
            endChat();
            return;
        }
        
        // 玩家接过任务
        if (role == Role.both || role == Role.end) {
            boolean completed = taskService.checkCompletion(player, task);
            if (completed) {
                // 任务完成的对话
                Talk talk = new TalkImpl();
                for (String s : ResourceManager.getTaskChatEnd(taskId)) {
                    talk.face(actor, player, false);
                    talk.speak(actor, s);
                }
                talk.addListener(new TalkListener() {
                    @Override
                    public void onTalkEnd() {
                        userCommandNetwork.chatTaskComplete(player, task);
                        playService.addMessage(ResourceManager.get(ResConstants.TASK_SUCCESS)
                                + ":" + ResourceManager.getObjectName(taskId)
                                , MessageType.notice);
                        endChat();
                    }
                });
                actorService.talk(talk);
            } else {
                // 任务未完成，进行询问
                Talk talk = new TalkImpl();
                String[] asks = ResourceManager.getTaskChatAsk(taskId);
                talk.face(actor, player, false);
                talk.speak(actor, asks[FastMath.nextRandomInt(0, asks.length - 1)]);
                actorService.talk(talk);
                endChat();
            }
        }
    }
    
    private void createRequestPanel(Task task) {
        if (requestPanel == null) {
            requestPanel = new TaskRequestPanel(width, height);
            requestPanel.setTitle(getChatName());
            requestPanel.setTaskDetails(ResourceManager.getObjectDes(taskId));
            requestPanel.resize();
            requestPanel.setToCorner(UI.Corner.CC);
        }
    }
    
    // 接受任务
    private void taskAccept() {
        userCommandNetwork.chatTaskAdd(player, task);
        requestPanel.close();
        playService.addMessage(ResourceManager.get(ResConstants.TASK_ACCEPT)
                + ": " + ResourceManager.getObjectName(taskId)
                , MessageType.notice);
        endChat();
    }
    
    // 拒绝任务
    private void taskReject() {
        requestPanel.close();
        endChat();
    }
    
    @Override
    public String getChatName() {
        if (chatName == null) {
            chatName = ResourceManager.get(ResConstants.TASK_TASK) 
                    + "-" + ResourceManager.getObjectName(taskId);
        }
        return chatName;
    }
    
    // -------------------------------------------------------------------------
    
    private class TaskRequestPanel extends Window{
        // 任务内容
        private Text text;
        private ButtonPanel btnPanel;

        public TaskRequestPanel(float width, float height) {
            super(width, height);
            text = new Text("");
            text.setWidth(width);
            btnPanel = new ButtonPanel(width, UIFactory.getUIConfig().getButtonHeight(), new String[] {
                ResourceManager.get(ResConstants.TASK_REJECT)
                ,ResourceManager.get(ResConstants.TASK_ACCEPT)
            });
            btnPanel.addClickListener(0, new Listener() {
                @Override
                public void onClick(UI view, boolean isPressed) {
                    if (isPressed) return;
                    taskReject();
                }
            });
            btnPanel.addClickListener(1, new Listener() {
                @Override
                public void onClick(UI view, boolean isPressed) {
                    if (isPressed) return;
                    taskAccept();
                }
            });
            addView(text);
            addView(btnPanel);
        }
        
        private void setTaskDetails(String details) {
            text.setText(details);
        }
        
    }
   
}
