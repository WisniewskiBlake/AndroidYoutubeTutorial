package com.example.networkingapp.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.example.networkingapp.R;
import com.example.networkingapp.adapter.ProfileViewPagerAdapter;
import com.example.networkingapp.model.User;
import com.example.networkingapp.rest.ApiClient;
import com.example.networkingapp.rest.services.UserInterface;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {

    @BindView(R.id.profile_cover)
    ImageView profileCover;
    @BindView(R.id.profile_image)
    CircleImageView profileImage;
    @BindView(R.id.profile_option_btn)
    Button profileOptionBtn;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.ViewPager_profile)
    ViewPager ViewPagerProfile;

    ProfileViewPagerAdapter profileViewPagerAdapter;

    int current_state = 0;
    String profileUrl = "";
    String coverUrl = "";
    String uid = "0";
    ProgressDialog progressDialog;

    int imageUploadType = 0;
    File compressedImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);


        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back_white);

        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            }
        });
        uid = getIntent().getStringExtra("uid");

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equalsIgnoreCase(uid)) {
            // UID is matched , we are going to load our own profile
            current_state = 5;
            profileOptionBtn.setText("Edit Profile");
            loadProfile();
        } else {

            //otherOthersProfile();
            // load others profile here
        }

        profileOptionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileOptionBtn.setEnabled(false);
                if(current_state == 5) {
                    CharSequence options[] = new CharSequence[]{"Change Cover Picture", "Change Profile Picture", "View Cover Picture", "View Profile Picture", "Sign Out"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setOnDismissListener(ProfileActivity.this);
                    builder.setTitle("Choose Options");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            if (position == 0) {
                                imageUploadType = 1;
                                ImagePicker.create(ProfileActivity.this)
                                        .folderMode(true)
                                        .single()
                                        .toolbarFolderTitle("Choose a folder")
                                        .toolbarImageTitle("Select a Image")
                                        .start();
                                //Change cover part
                            } else if (position == 1) {
                                imageUploadType = 0;
                                ImagePicker.create(ProfileActivity.this)
                                        .folderMode(true)
                                        .single().toolbarFolderTitle("Choose a folder").toolbarImageTitle("Select a Image")
                                        .start();
                                //Change  profile part
                            } else if (position == 2) {
                                //viewFullImage(profileCover, coverUrl);
                                //view cover proifle
                            } else if (position == 3) {
                                //viewFullImage(profileImage, profileUrl);
                                //view profile picture
                            } else {
                                //signOut();
                            }
                        }
                    });
                    builder.show();
                }
            }
        });


    }

    private void loadProfile() {
        UserInterface userInterface = ApiClient.getApiClient().create(UserInterface.class);
        Map<String, String> params = new HashMap<>();
        params.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        Call<User> call = userInterface.loadownProfile(params);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    showUserData(response.body());

                } else {
                    Toast.makeText(ProfileActivity.this, "Something went wrong ... Please try later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Something went wrong ... Please try later", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void showUserData(User user) {
        profileViewPagerAdapter = new ProfileViewPagerAdapter(getSupportFragmentManager(), 1);
        ViewPagerProfile.setAdapter(profileViewPagerAdapter);

        coverUrl = user.getCoverUrl();
        profileUrl = user.getProfileUrl();
        collapsingToolbar.setTitle(user.getName());

        if (!profileUrl.isEmpty()) {
            Picasso.get().load(profileUrl).into(profileImage, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {

                }
                @Override
                public void onError(Exception e) {
                    Picasso.get().load(profileUrl).into(profileImage);
                }
            });

            if (!coverUrl.isEmpty()) {
                Picasso.get().load(coverUrl).into(profileCover, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {

                    }
                    @Override
                    public void onError(Exception e) {
                        System.out.println("Error");
                        Picasso.get().load(coverUrl).into(profileCover);
                    }
                });
            }

        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        profileOptionBtn.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image selectedImage = ImagePicker.getFirstImageOrNull(data);
            try{
                compressedImageFile = new Compressor(this)
                        .setQuality(75)
                        .compressToFile(new File(selectedImage.getPath()));

                uploadFile(compressedImageFile);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadFile(final File compressedImageFile) {
        progressDialog.setTitle("Loading...");
        progressDialog.show();

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("postUserId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        builder.addFormDataPart("imageUploadType", imageUploadType + "");
        builder.addFormDataPart("file", compressedImageFile.getName(),
                RequestBody.create(MediaType.parse("multipart/form-data"), compressedImageFile));

        MultipartBody multipartBody = builder.build();
        UserInterface userInterface = ApiClient.getApiClient().create(UserInterface.class);

        Call<Integer> call = userInterface.uploadImage(multipartBody);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                progressDialog.dismiss();
                if (response.body() != null && response.body() == 1) {
                    if (imageUploadType == 0) {
                        Picasso.get().load(compressedImageFile).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.default_image_placeholder).into(profileImage, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(compressedImageFile)
                                        .placeholder(R.drawable.default_image_placeholder).into(profileImage);
                            }
                        });
                        Toast.makeText(ProfileActivity.this, "Profile Picture Changed Successfully", Toast.LENGTH_LONG).show();
                    } else {
                        Picasso.get().load(compressedImageFile).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.default_image_placeholder).into(profileCover, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(compressedImageFile).placeholder(R.drawable.default_image_placeholder).into(profileCover);
                            }
                        });
                        Toast.makeText(ProfileActivity.this, "Cover Picture Changed Successfully", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });




    }













}
