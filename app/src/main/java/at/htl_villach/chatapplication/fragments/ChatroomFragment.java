package at.htl_villach.chatapplication.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import at.htl_villach.chatapplication.GroupInfoActivity;
import at.htl_villach.chatapplication.R;
import at.htl_villach.chatapplication.adapters.ChatroomAdapter;
import at.htl_villach.chatapplication.bll.Chat;
import at.htl_villach.chatapplication.bll.Message;

import static android.support.constraint.Constraints.TAG;

public class ChatroomFragment extends Fragment {

    public static int TOTAL_ITEMS_TO_LOAD = 10;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_PICK_PHOTO = 2;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final int PICK_IMAGE = 1;
    public static final int TAKE_PICTURE = 2;

    //controlls
    private EditText messageToSend;
    private ImageButton btnSend;
    private ImageButton btnSendPicture;
    private RecyclerView recyclerViewMessages;
    private ChatroomAdapter chatroomAdapter;
    private LinearLayoutManager linearLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;

    //Database
    private FirebaseUser fuser;
    private DatabaseReference mRootRef;
    private StorageReference storageReference;
    private ValueEventListener valueEventListener;
    private HashMap<DatabaseReference, ValueEventListener> databaseListeners = new HashMap<>();

    //data
    private Chat mCurrentChat;
    private List<Message> mMessages = new ArrayList<>();
    private int mCurrentPage = 1;
    private int mItemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    private Uri imageUri;

    public ChatroomFragment() {
        // Required empty public constructor
    }

    public static ChatroomFragment newInstance(Chat selectedChat) {
        ChatroomFragment toDoListFragment = new ChatroomFragment();

        Bundle args = new Bundle();
        args.putParcelable("selectedChat", selectedChat);
        toDoListFragment.setArguments(args);

        return toDoListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentChat = getArguments().getParcelable("selectedChat");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatroom, container, false);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        messageToSend = (EditText) view.findViewById(R.id.message_to_send);
        messageToSend.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    btnSendPicture.setVisibility(View.VISIBLE);
                } else {
                    btnSendPicture.setVisibility(View.GONE);
                }
            }
        });


        btnSend = (ImageButton) view.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = messageToSend.getText().toString();

                if (!msg.trim().equals("")) {
                    sendMessage(msg.trim());
                } else {
                    Toast.makeText(getActivity(), R.string.emptyMessage, Toast.LENGTH_SHORT).show();
                }

                messageToSend.setText("");
            }
        });

        btnSendPicture = (ImageButton) view.findViewById(R.id.btn_sendPicture);
        btnSendPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = {"Choose from Gallery", "Open Camera"};
                AlertDialog.Builder builder = new AlertDialog.Builder((Activity) getContext());
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                checkPermissionAndPickPhotoIfGranted();
                                break;
                            case 1:
                                checkPermissionAndTakePhotoIfGranted();
                                break;
                            default:
                                Toast.makeText(getContext(), "Something unexpected happened", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                builder.show();
            }
        });

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);

        chatroomAdapter = new ChatroomAdapter(getContext(), mMessages, mCurrentChat);

        recyclerViewMessages = (RecyclerView) view.findViewById(R.id.recycler_view_messages);
        recyclerViewMessages.setNestedScrollingEnabled(false);

        recyclerViewMessages.setLayoutManager(linearLayoutManager);
        recyclerViewMessages.setAdapter(chatroomAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                mItemPos = 0;

                readMoreMessages();
            }
        });

        readMessages();
        updateSeenMessage();

        return view;
    }

    private void readMoreMessages() {
        DatabaseReference messagesRef = mRootRef.child("Messages").child(mCurrentChat.getId());
        Query messageQuery = messagesRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);

                if (!mPrevKey.equals(message.getId())) {
                    mMessages.add(mItemPos++, message);
                } else {
                    mPrevKey = mLastKey;
                }

                if (mItemPos == 1) {
                    mLastKey = message.getId();
                }

                if (!message.getSender().equals(fuser.getUid())) {
                    mRootRef.child("MessagesSeenBy").child(mCurrentChat.getId()).child(message.getId()).child(fuser.getUid()).setValue(true);
                }

                chatroomAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);

                //todo: springen zur richtigen position
                //linearLayoutManager.scrollToPositionWithOffset(0, 10);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Message m = dataSnapshot.getValue(Message.class);

                mMessages.remove(m);
                chatroomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        DatabaseReference messagesRef = mRootRef.child("Messages").child(mCurrentChat.getId());
        Query messageQuery = messagesRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addValueEventListener(valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMessages.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Message message = ds.getValue(Message.class);

                    mItemPos++;

                    if (mItemPos == 1) {
                        mLastKey = message.getId();
                        mPrevKey = mLastKey;
                    }

                    if (!message.getSender().equals(fuser.getUid()) && !message.isIsseen()) {
                        mRootRef.child("MessagesSeenBy").child(mCurrentChat.getId()).child(message.getId()).child(fuser.getUid()).setValue(true);
                    }

                    mMessages.add(message);
                }
                chatroomAdapter.notifyDataSetChanged();

                recyclerViewMessages.scrollToPosition(mMessages.size() - 1);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseListeners.put(messagesRef, valueEventListener);
    }

    private void updateSeenMessage() {
        DatabaseReference seenMessagesRef = FirebaseDatabase.getInstance().getReference("MessagesSeenBy").child(mCurrentChat.getId());
        seenMessagesRef.addValueEventListener(valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, HashMap<String, Object>> messageSeenBy = (HashMap<String, HashMap<String, Object>>) dataSnapshot.getValue();

                for (Message m : mMessages) {
                    if (!m.isIsseen()) {
                        if (!messageSeenBy.get(m.getId()).containsValue(false)) {
                            mRootRef.child("Messages").child(mCurrentChat.getId()).child(m.getId()).child("isseen").setValue(true);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseListeners.put(seenMessagesRef, valueEventListener);
    }

    private void sendMessage(String message) {
        DatabaseReference sendMessagesRef = mRootRef.child("Messages").child(mCurrentChat.getId());
        String messageId = sendMessagesRef.push().getKey();

        HashMap<String, Object> hashMapMessage = new HashMap<>();
        hashMapMessage.put("id", messageId);
        hashMapMessage.put("sender", fuser.getUid());
        hashMapMessage.put("type", "text");
        hashMapMessage.put("message", message);
        hashMapMessage.put("timestamp", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        hashMapMessage.put("isseen", false);

        sendMessagesRef.child(messageId).updateChildren(hashMapMessage);

        HashMap<String, Object> hashMapMessageSeenBy = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : mCurrentChat.getUsers().entrySet()) {
            if (!entry.getKey().equals(fuser.getUid())) {
                hashMapMessageSeenBy.put(entry.getKey(), false);
            }
        }
        mRootRef.child("MessagesSeenBy").child(mCurrentChat.getId()).child(messageId).setValue(hashMapMessageSeenBy);
    }

    private void checkPermissionAndTakePhotoIfGranted() {
        int permission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_STORAGE, REQUEST_TAKE_PHOTO);
        } else {
            captureImage();
        }
    }

    private void checkPermissionAndPickPhotoIfGranted() {
        int permission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_STORAGE, REQUEST_PICK_PHOTO);
        } else {
            pickImage();
        }
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + ".jpg");
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose a Picture"), PICK_IMAGE);
    }

    private void saveImageToDatabase() {
        DatabaseReference sendMessagesRef = mRootRef.child("Messages").child(mCurrentChat.getId());
        String messageId = sendMessagesRef.push().getKey();

        HashMap<String, Object> hashMapMessage = new HashMap<>();
        hashMapMessage.put("id", messageId);
        hashMapMessage.put("sender", fuser.getUid());
        hashMapMessage.put("type", "image");
        hashMapMessage.put("message", "message_images/" + mCurrentChat.getId() + "/" + messageId + ".jpg");
        hashMapMessage.put("timestamp", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        hashMapMessage.put("isseen", false);

        sendMessagesRef.child(messageId).updateChildren(hashMapMessage);

        HashMap<String, Object> hashMapMessageSeenBy = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : mCurrentChat.getUsers().entrySet()) {
            if (!entry.getKey().equals(fuser.getUid())) {
                hashMapMessageSeenBy.put(entry.getKey(), false);
            }
        }
        mRootRef.child("MessagesSeenBy").child(mCurrentChat.getId()).child(messageId).setValue(hashMapMessageSeenBy);

        storageReference.child("message_images/" + mCurrentChat.getId() + "/" + messageId + ".jpg").putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getContext(), "Image send", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Bitmap createSquaredBitmap(Bitmap source) {
        int dim = Math.max(source.getWidth(), source.getHeight());
        Bitmap dstBmp = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(dstBmp);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(source, (dim - source.getWidth()) / 2, (dim - source.getHeight()) / 2, null);

        return dstBmp;
    }


    private void checkOrientation() throws Exception {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        String filePath;

        Cursor cursor = getContext().getContentResolver().query(imageUri, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        } else {
            filePath = imageUri.getPath();
        }


        ExifInterface exif = new ExifInterface(filePath);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        bitmap = createSquaredBitmap(bitmap);

        if (orientation == 6) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            FileOutputStream out = new FileOutputStream(filePath);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, out);
        } else {
            FileOutputStream out = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, out);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == PICK_IMAGE) {
                    imageUri = data.getData();
                    checkOrientation();
                    saveImageToDatabase();
                    Toast.makeText(getContext(), "geht", Toast.LENGTH_LONG).show();
                } else if (requestCode == TAKE_PICTURE) {
                    checkOrientation();
                    saveImageToDatabase();
                    Toast.makeText(getContext(), "geht", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception ex) {
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        for (Map.Entry<DatabaseReference, ValueEventListener> entry : databaseListeners.entrySet()) {
            DatabaseReference databaseReference = entry.getKey();
            ValueEventListener value = entry.getValue();
            databaseReference.removeEventListener(value);
        }
    }
}
