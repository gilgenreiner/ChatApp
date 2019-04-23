package at.htl_villach.chatapplication;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import at.htl_villach.chatapplication.adapters.PagerAdapter;
import at.htl_villach.chatapplication.fragments.contacts;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TabLayout tbMain = findViewById(R.id.tbMain);

        tbMain.setNestedScrollingEnabled(false);

        firebaseAuth = FirebaseAuth.getInstance();
        //tbMain.setTabGravity(TabLayout.GRAVITY_FILL);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_toolbar);
        toolbar.setTitle("ChatApp");

        final ViewPager pager = findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tbMain.getTabCount());
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tbMain));


        tbMain.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //warsch switch
                if(menuItem.getItemId() == R.id.mnLogout) {
                    firebaseAuth.signOut();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.putExtra("allowBack", false);
                    startActivity(intent);
                    finish();
                    return true;
                }
                else if(menuItem.getItemId() == R.id.mnRefresh) {
                    Fragment fragment = adapter.getCurrentFragment();
                    if(fragment instanceof contacts) {
                        contacts frContacts = (contacts) fragment;
                        frContacts.RefreshList();
                    }
                }
                else if(menuItem.getItemId() == R.id.mnEditProfile) {
                    Toast.makeText(MainActivity.this, "Temporary toast for menuItem Edit!",
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                else if(menuItem.getItemId() == R.id.mnDeleteProfile) {
                    Toast.makeText(MainActivity.this, "Temporary toast for menuItem Delete!",
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });



        final FloatingActionButton btnAddUser = findViewById(R.id.btnAddUser);

        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddUserActivity.class);
                startActivity(intent);
            }
        });

    }
}
