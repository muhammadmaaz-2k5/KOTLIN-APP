package com.job2day.nazaarabox.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.job2day.nazaarabox.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.SurfaceDark
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BackgroundDark)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PrivacyCard {
                    Column {
                        Text(
                            text = "Privacy Policy",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BodyText("This Privacy Policy describes how Nazaarabox collects, uses, and protects your information when you use our application.")
                    }
                }

                PrivacyCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle("1. Information We Collect")
                        BodyText("We may collect the following information when you use the app:")
                        BulletText("Device information such as device model, operating system version, and unique device identifiers")
                        BulletText("Usage data including which dramas and episodes you view")
                        BulletText("Advertising identifiers for serving personalized ads")
                        BulletText("Notification tokens for push notifications")

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(8.dp))

                        SectionTitle("2. How We Use Your Information")
                        BodyText("We use the collected information to:")
                        BulletText("Provide and maintain the streaming service")
                        BulletText("Send push notifications about new content and updates")
                        BulletText("Serve advertisements through Google AdMob")
                        BulletText("Improve app performance and user experience")
                    }
                }

                PrivacyCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle("3. Advertising")
                        BodyText("This app uses Google AdMob to serve advertisements. AdMob may collect and process your advertising identifier and device information to deliver personalized ads. You can opt out of personalized advertising in your device settings.")

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(8.dp))

                        SectionTitle("4. Push Notifications")
                        BodyText("We use Firebase Cloud Messaging and OneSignal to send push notifications. We collect notification interaction data to improve our notification service.")

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(8.dp))

                        SectionTitle("5. Data Sharing")
                        BodyText("We do not sell your personal data. We may share anonymized data with third-party service providers (Firebase, OneSignal, AdMob) solely for the purpose of operating the app.")
                    }
                }

                PrivacyCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle("6. 18+ Content Notice")
                        BodyText("This app contains mature content intended for users aged 18 and above. By using this app, you confirm that you are at least 18 years old. Parental guidance is advised for users under 18.")

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(8.dp))

                        SectionTitle("7. Data Security")
                        BodyText("We implement reasonable security measures to protect your information. However, no method of electronic storage or transmission is 100% secure.")

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(8.dp))

                        SectionTitle("8. Your Rights")
                        BodyText("You can disable push notifications at any time through your device settings. You can reset your advertising identifier through your device settings. You can stop using the app at any time.")
                    }
                }

                PrivacyCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle("9. Contact")
                        BodyText("If you have any questions about this Privacy Policy, please contact us through the app's Discord server.")

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(8.dp))

                        SectionTitle("10. Changes to This Policy")
                        BodyText("We may update this Privacy Policy from time to time. Continued use of the app after changes constitutes acceptance of the updated policy.")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PrivacyCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun BodyText(text: String) {
    Text(
        text = text,
        color = Color.LightGray,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
}

@Composable
private fun BulletText(text: String) {
    Row(
        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
    ) {
        Text(
            text = "\u2022",
            color = Color.LightGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            color = Color.LightGray,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}
