package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.task.AbstractTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class LinkedHashMapCustom {
    private NodeCustom head;
    private NodeCustom tail;
    private NodeCustom newNode;
    private NodeCustom currentNode;
    private NodeCustom previousNode;
    private NodeCustom nextNode;
    private final HashMap<Integer, NodeCustom> innerHashMap;

    private static class NodeCustom {
        AbstractTask task;
        NodeCustom previousNode; //
        NodeCustom nextNode;

        NodeCustom (AbstractTask task, NodeCustom previous, NodeCustom next) {
            this.task = task;
            previousNode = previous;
            nextNode = next;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;

            NodeCustom nodeCustom = (NodeCustom) object;
            Integer thisPreviousTaskIdNumber = null;
            Integer nodeCustomPreviousTaskIdNumber = null;
            Integer thisNextTaskIdNumber = null;
            Integer nodeCustomNextTaskIdNumber = null;

            if (previousNode != null) {
                thisPreviousTaskIdNumber = previousNode.task.getIdNumber();
            }

            if (nodeCustom.previousNode != null) {
                nodeCustomPreviousTaskIdNumber = nodeCustom.previousNode.task.getIdNumber();
            }

            if (nextNode != null) {
                thisNextTaskIdNumber = nextNode.task.getIdNumber();
            }

            if (nodeCustom.nextNode != null) {
                nodeCustomNextTaskIdNumber = nodeCustom.nextNode.task.getIdNumber();
            }

            return task.getIdNumber() ==  nodeCustom.task.getIdNumber()
                    && thisPreviousTaskIdNumber == nodeCustomPreviousTaskIdNumber
                    && thisNextTaskIdNumber == nodeCustomNextTaskIdNumber;
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(task.getIdNumber());

            Integer previousTaskIdNumber = null;
            Integer nextTaskIdNumber = null;

            if (previousNode != null) {
                previousTaskIdNumber = previousNode.task.getIdNumber();
            }

            if (nextNode != null) {
                nextTaskIdNumber = nextNode.task.getIdNumber();
            }

            result = 31 * result + Objects.hashCode(previousTaskIdNumber);

            result = 31 * result + Objects.hashCode(nextTaskIdNumber);

            return result;
        }
    }

    public LinkedHashMapCustom() {
        head = null;
        tail = null;
        innerHashMap = new HashMap<>();
    }

    public LinkedHashMapCustom createCopy() {
        if (this == null) {
            return new LinkedHashMapCustom();
        }

        LinkedHashMapCustom copyHistory = new LinkedHashMapCustom();

        for (NodeCustom node : this.innerHashMap.values()) {
            copyHistory.addLast(node.task);
        }

        return copyHistory;
    }

    public ResultOfOperation addLast(AbstractTask task) {
        if (task == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (task.getName() == null || task.getDescription() == null
                || task.getStatusProgress() == null || task.getIdNumber() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        } else if (task.getIdNumber() < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        }

        if (innerHashMap.containsKey(task.getIdNumber())) {
            removeEntry(task.getIdNumber());
        }

        previousNode = tail;
        newNode = new NodeCustom(task, tail, null);
        tail = newNode;

        if (previousNode == null) {
            head = newNode;
        } else {
            previousNode.nextNode = newNode;
        }

        innerHashMap.put(task.getIdNumber(), newNode);
        return ResultOfOperation.SUCCESS;
    }

    public ResultOfOperation removeEntry(Integer taskIdNumber) {
        if (taskIdNumber == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (taskIdNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        }

        if (innerHashMap.containsKey(taskIdNumber)) {
            currentNode = innerHashMap.get(taskIdNumber);
            previousNode = currentNode.previousNode;
            nextNode = currentNode.nextNode;

            if (previousNode == null) {
                head = currentNode.nextNode;
            } else {
                previousNode.nextNode = nextNode;
            }

            if (nextNode == null) {
                tail = previousNode;
            } else {
                nextNode.previousNode = previousNode;
            }

            currentNode = null;
            previousNode = null;
            nextNode = null;
            innerHashMap.remove(taskIdNumber);
            return ResultOfOperation.SUCCESS;
        }
        return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
    }

    public ArrayList<AbstractTask> getListAllItem() {

        if (innerHashMap.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<AbstractTask> listForReturn = new ArrayList<>(innerHashMap.size());
        NodeCustom nodeForList = head;

        while (true) {
            listForReturn.add(nodeForList.task);
            if (nodeForList.nextNode == null) {
                return listForReturn;
            }
            nodeForList = nodeForList.nextNode;
        }
    }

    public Integer getSize() {
        return innerHashMap.size();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        LinkedHashMapCustom history = (LinkedHashMapCustom) object;

        return Objects.equals(head, history.head)
                && Objects.equals(tail, history.tail)
                && Objects.equals(innerHashMap, history.innerHashMap);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(head);
        result = 31 * result + Objects.hashCode(tail);
        result = 31 * result + Objects.hashCode(innerHashMap);

        return result;
    }
}
