package com.allen_anker.charter;

import com.allen_anker.charter.view.CharterFrame;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) {
        ReentrantLock lock = new ReentrantLock();
        Condition readingCondition = lock.newCondition();
        lock.lock();
        lock.unlock();
        new CharterFrame();
    }
}