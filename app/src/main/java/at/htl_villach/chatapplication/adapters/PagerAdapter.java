package at.htl_villach.chatapplication.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import at.htl_villach.chatapplication.fragments.chats;
import at.htl_villach.chatapplication.fragments.contacts;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int numberTabs;

    public PagerAdapter(FragmentManager fm, int numTabs) {
        super(fm);
        this.numberTabs = numTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                contacts contactSection = new contacts();
                return contactSection;

            case 1:
                chats chatSection = new chats();
                return chatSection;

            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return numberTabs;
    }
}
