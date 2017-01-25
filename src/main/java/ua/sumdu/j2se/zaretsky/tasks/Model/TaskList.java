package ua.sumdu.j2se.zaretsky.tasks.model;

import java.io.Serializable;

/**
 * Class for creating and editing TaskList
 *
 * @author Nikolay Zaretsky
 * @version 1.0 25 Oct 2016
 */
abstract public class TaskList implements Iterable<Task>, Cloneable, Serializable {
    protected int count = 0; //Count of task in TaskList

    public TaskList getTaskList() {
        return this;
    }

    /**
     * Method to add task into TaskList
     *
     * @param task - current task for adding
     */
    public abstract void add(Task task);

    /**
     * Method to remove task from TaskList
     *
     * @param task - current task for removing
     * @return true  if this task was in TaskList
     */
    public abstract boolean remove(Task task);

    /**
     * Method to getting task from TaskList
     *
     * @param index - getting task with current index from TaskList
     * @return Task
     */
    public abstract Task getTask(int index);

    /**
     * Method to get size TaskList
     *
     * @return size of TaskList(with tasks)
     */
    public int size() {
        return count;
    }


    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) return true;
        if (!(otherObject instanceof TaskList)) return false;

        TaskList that = (TaskList) otherObject;

        if (size() != that.size()) return false;
        for (int i = 0; i < size(); i++) {
            if (!this.getTask(i).toString().equals(that.getTask(i).toString())) {
                return false;
            }
        }

        return true;
    }


    @Override
    public int hashCode() {

        int result = count;

        for (int i = 0; i < count; i++) {
            result = 31 * result + this.getTask(i).hashCode();
        }
        return result;

    }

    /*@Override
    public final TaskList clone() throws CloneNotSupportedException {
        return (TaskList) super.clone();
    }*/

 /*   private void writeObject(ObjectOutputStream out) throws IOException {
//        out.defaultWriteObject();
        out.writeInt(this.count);
        for (Task task : (Iterable<Task>) this) {
            out.writeInt(task.getTitle().length());
            out.writeObject(task.getTitle());
            out.writeInt(task.isActive() ? 1 : 0);
            out.writeInt(task.getRepeatInterval());
            if(task.isRepeated()){
                out.writeLong(task.getStartTime().getTime());
                out.writeLong(task.getEndTime().getTime());
            }
            else {
                out.writeLong(task.getTime().getTime());
            }
        }
    }*/

/*    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//        in.defaultReadObject();
        int a = in.readInt();
        for (int i = 0; i < a ; i++) {
            int lengthTitle = in.readInt();
            String title = (String) in.readObject();
            boolean active = in.readInt()==1? true:false;
            int repeat = in.readInt();
            if (repeat==0){
                long startTime = in.readLong();
                Task task = new Task(title, new Date(startTime));
                task.setActive(active);
                this.add(task);
            }
            else {
                long startTime = in.readLong();
                long endTime = in.readLong();
                Task task = new Task(title, new Date(startTime), new Date
                        (endTime), repeat);
                task.setActive(active);
                this.add(task);
            }
        }

    }*/
}
