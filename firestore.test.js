const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_EMAIL = "majidboushi777@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

const ROLES = {
  REPAIR_TECH: "تعمیرکار",
  SOFTWARE_TECH: "نرم\u200cافزارکار",
  MOBILE_SELLER: "فروشنده موبایل",
  ACCESSORY_SELLER: "فروشنده جانبی",
  WHOLESALE_MOBILE: "عمده\u200cفروش موبایل",
  WHOLESALE_ACCESSORY: "عمده\u200cفروش جانبی",
  PART_SELLER: "قطعه‌فروش",
};

let testEnv;

function dbFor(uid, email = null, emailVerified = true) {
  if (!uid) return testEnv.unauthenticatedContext().firestore();
  const claims = email ? { email, email_verified: emailVerified } : {};
  return testEnv.authenticatedContext(uid, claims).firestore();
}

async function seedInvitedActiveUser(adminDb, uid, email, tier, extra = {}) {
  const normEmail = email.toLowerCase();
  const now = new Date(Date.now() - 5000);
  await assertSucceeds(
    adminDb.collection("user_invites").doc(normEmail).set({
      email: normEmail,
      fullName: extra.fullName || "کاربر تست",
      phone: extra.phone || "09120001122",
      tier,
      province: extra.province || "تهران",
      city: extra.city || "تهران",
      region: extra.region || "علاءالدین و جمهوری",
      active: true,
      createdDateLabel: "امروز",
      sourceUserDocId: "",
      claimedByUid: uid,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    adminDb.collection("users").doc(uid).set({
      uid,
      email: normEmail,
      fullName: extra.fullName || "کاربر تست",
      phone: extra.phone || "09120001122",
      tier,
      province: extra.province || "تهران",
      city: extra.city || "تهران",
      region: extra.region || "علاءالدین و جمهوری",
      active: extra.active !== undefined ? extra.active : true,
      createdDateLabel: "امروز",
      createdAt: now,
      updatedAt: now,
    })
  );
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

test("Unauthenticated users can submit pending registration requests only and cannot access collections", async () => {
  const unauth = dbFor(null);
  const adminDb = dbFor("admin_uid", ADMIN_EMAIL, true);
  const now = new Date(Date.now() - 2000);

  await assertFails(unauth.collection("stores").doc("st1").get());
  await assertFails(unauth.collection("stores").where("published", "==", true).get());
  await assertFails(
    unauth.collection("users").doc("u1").set({
      uid: "u1",
      fullName: "کاربر",
      tier: ROLES.REPAIR_TECH,
      province: "تهران",
      city: "تهران",
      region: "همه مناطق",
      active: true,
      createdAt: now,
    })
  );

  // Unauthenticated user CAN submit a pending registration request
  await assertSucceeds(
    unauth.collection("registration_requests").doc("req_1").set({
      email: "new.partner@gmail.com",
      fullName: "محمدحسین کریمی",
      phone: "09123456789",
      requestedTier: ROLES.WHOLESALE_MOBILE,
      province: "تهران",
      city: "تهران",
      region: "علاءالدین و جمهوری",
      note: "دارای جواز کسب در علاءالدین",
      status: "pending",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Cannot self-approve or pick legacy role "سخت‌افزارکار"
  await assertFails(
    unauth.collection("registration_requests").doc("req_2").set({
      email: "bad@gmail.com",
      fullName: "شخص",
      phone: "09123456789",
      requestedTier: ROLES.REPAIR_TECH,
      province: "تهران",
      city: "تهران",
      region: "همه مناطق",
      status: "approved",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    unauth.collection("registration_requests").doc("req_3").set({
      email: "bad2@gmail.com",
      fullName: "شخص",
      phone: "09123456789",
      requestedTier: "سخت‌افزارکار",
      province: "تهران",
      city: "تهران",
      region: "همه مناطق",
      status: "pending",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Unauthenticated cannot read/list/update/delete registration_requests
  await assertFails(unauth.collection("registration_requests").doc("req_1").get());
  await assertFails(unauth.collection("registration_requests").get());
  await assertFails(unauth.collection("registration_requests").doc("req_1").update({ status: "approved" }));
  await assertFails(unauth.collection("registration_requests").doc("req_1").delete());

  // Admin can list and approve registration requests
  await assertSucceeds(adminDb.collection("registration_requests").get());
  await assertSucceeds(
    adminDb.collection("registration_requests").doc("req_1").update({
      status: "approved",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Invite-only allowlist blocks uninvited Google sign-in and lets invited users claim exact assigned role", async () => {
  const strangerDb = dbFor("stranger_uid", "stranger@gmail.com", true);
  const aliceDb = dbFor("alice_uid", "Alice@Example.com", true);
  const adminDb = dbFor("admin_uid", ADMIN_EMAIL, true);
  const now = new Date(Date.now() - 5000);

  // Stranger without an invite cannot create /users profile or read catalog
  await assertFails(
    strangerDb.collection("users").doc("stranger_uid").set({
      uid: "stranger_uid",
      email: "stranger@gmail.com",
      fullName: "غریبه",
      phone: "0912",
      tier: ROLES.REPAIR_TECH,
      province: "تهران",
      city: "تهران",
      region: "همه مناطق",
      active: true,
      createdAt: now,
    })
  );
  await assertFails(strangerDb.collection("mobiles").where("published", "==", true).get());

  // Non-admin cannot create invites, and Admin cannot assign legacy "سخت‌افزارکار" role
  await assertFails(
    aliceDb.collection("user_invites").doc("alice@example.com").set({
      email: "alice@example.com",
      fullName: "علی",
      phone: "0912",
      tier: ROLES.MOBILE_SELLER,
      province: "تهران",
      city: "تهران",
      region: "همه مناطق",
      active: true,
      createdAt: now,
    })
  );
  await assertFails(
    adminDb.collection("user_invites").doc("alice@example.com").set({
      email: "alice@example.com",
      fullName: "علی",
      phone: "0912",
      tier: "سخت‌افزارکار",
      province: "تهران",
      city: "تهران",
      region: "همه مناطق",
      active: true,
      createdAt: now,
    })
  );

  // Admin creates email-keyed invite for Alice as فروشنده موبایل + placeholder user doc
  await assertSucceeds(
    adminDb.collection("users").doc("u_tmp_alice").set({
      uid: "u_tmp_alice",
      email: "alice@example.com",
      fullName: "علی رضایی",
      phone: "09120001122",
      tier: ROLES.MOBILE_SELLER,
      province: "اصفهان",
      city: "اصفهان",
      region: "فردوسی و بازار موبایل",
      active: true,
      createdDateLabel: "امروز",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    adminDb.collection("user_invites").doc("alice@example.com").set({
      email: "alice@example.com",
      fullName: "علی رضایی",
      phone: "09120001122",
      tier: ROLES.MOBILE_SELLER,
      province: "اصفهان",
      city: "اصفهان",
      region: "فردوسی و بازار موبایل",
      active: true,
      createdDateLabel: "امروز",
      sourceUserDocId: "u_tmp_alice",
      claimedByUid: "",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Alice reads her invite
  await assertSucceeds(aliceDb.collection("user_invites").doc("alice@example.com").get());

  // Alice CANNOT create her profile with a different tier (e.g., عمده‌فروش موبایل)
  await assertFails(
    aliceDb.collection("users").doc("alice_uid").set({
      uid: "alice_uid",
      email: "alice@example.com",
      fullName: "علی رضایی",
      phone: "09120001122",
      tier: ROLES.WHOLESALE_MOBILE,
      province: "اصفهان",
      city: "اصفهان",
      region: "فردوسی و بازار موبایل",
      active: true,
      createdDateLabel: "امروز",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Alice creates her profile matching invite tier
  await assertSucceeds(
    aliceDb.collection("users").doc("alice_uid").set({
      uid: "alice_uid",
      email: "alice@example.com",
      fullName: "علی رضایی",
      phone: "09120001122",
      tier: ROLES.MOBILE_SELLER,
      province: "اصفهان",
      city: "اصفهان",
      region: "فردوسی و بازار موبایل",
      active: true,
      createdDateLabel: "امروز",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Alice claims invite & removes admin placeholder doc
  await assertSucceeds(
    aliceDb.collection("user_invites").doc("alice@example.com").update({
      claimedByUid: "alice_uid",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc("u_tmp_alice").delete());

  // Alice can update her own name/phone/location, CANNOT modify her tier or active flag
  await assertSucceeds(
    aliceDb.collection("users").doc("alice_uid").update({
      phone: "09129998877",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc("alice_uid").update({
      tier: ROLES.REPAIR_TECH,
    })
  );
  await assertFails(
    aliceDb.collection("users").doc("alice_uid").update({
      active: false,
    })
  );

  // Admin blocks Alice -> Alice loses access to catalog
  await assertSucceeds(adminDb.collection("users").doc("alice_uid").update({ active: false }));
  await assertFails(aliceDb.collection("mobiles").where("published", "==", true).get());

  // Admin reactivates Alice & updates her role -> Alice regains access
  await assertSucceeds(
    adminDb.collection("users").doc("alice_uid").update({
      active: true,
      tier: ROLES.WHOLESALE_MOBILE,
    })
  );
  await assertSucceeds(aliceDb.collection("mobiles").where("published", "==", true).get());

  // Admin can reactivate even legacy/incomplete user & invite docs missing createdAt
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const rawDb = ctx.firestore();
    await rawDb.collection("users").doc("legacy_user").set({
      uid: "legacy_doc_id_mismatch",
      email: "legacy@example.com",
      fullName: "کاربر قدیمی",
      tier: ROLES.REPAIR_TECH,
      province: "تهران",
      city: "تهران",
      region: "",
      active: false,
    });
    await rawDb.collection("user_invites").doc("legacy@example.com").set({
      email: "legacy@example.com",
      fullName: "کاربر قدیمی",
      tier: ROLES.REPAIR_TECH,
      province: "تهران",
      city: "تهران",
      region: "",
      active: false,
    });
  });
  await assertSucceeds(adminDb.collection("users").doc("legacy_user").update({ active: true }));
  await assertSucceeds(adminDb.collection("user_invites").doc("legacy@example.com").update({ active: true }));
});

test("Store registration allowed strictly for wholesale & part-seller roles", async () => {
  const adminDb = dbFor("admin_uid", ADMIN_EMAIL, true);
  const repairDb = dbFor("repair_uid", "repair@example.com");
  const mobileSellerDb = dbFor("mseller_uid", "mseller@example.com");
  const wholesaleMobDb = dbFor("wmobile_uid", "wmobile@example.com");
  const partSellerDb = dbFor("parts_uid", "parts@example.com");
  const now = new Date(Date.now() - 5000);

  await seedInvitedActiveUser(adminDb, "repair_uid", "repair@example.com", ROLES.REPAIR_TECH);
  await seedInvitedActiveUser(adminDb, "mseller_uid", "mseller@example.com", ROLES.MOBILE_SELLER);
  await seedInvitedActiveUser(adminDb, "wmobile_uid", "wmobile@example.com", ROLES.WHOLESALE_MOBILE);
  await seedInvitedActiveUser(adminDb, "parts_uid", "parts@example.com", ROLES.PART_SELLER);

  const baseStore = (ownerId, type) => ({
    name: "فروشگاه تست",
    province: "تهران",
    city: "تهران",
    region: "علاءالدین و جمهوری",
    phone: "021-66700000",
    address: "علاءالدین",
    hours: "۱۰ تا ۲۰",
    rating: "۵.۰",
    type,
    ownerId,
    published: true,
    createdAt: now,
  });

  // تعمیرکار and فروشنده موبایل CANNOT create stores
  await assertFails(repairDb.collection("stores").doc("st_rep").set(baseStore("repair_uid", "hw")));
  await assertFails(mobileSellerDb.collection("stores").doc("st_ms").set(baseStore("mseller_uid", "mobile")));

  // عمده‌فروش موبایل and قطعه‌فروش CAN create stores (with their own ownerId)
  await assertSucceeds(wholesaleMobDb.collection("stores").doc("st_wm").set(baseStore("wmobile_uid", "mobile")));
  await assertSucceeds(partSellerDb.collection("stores").doc("st_ps").set(baseStore("parts_uid", "hw")));

  // Cannot impersonate another ownerId when creating store
  await assertFails(wholesaleMobDb.collection("stores").doc("st_fake").set(baseStore("other_uid", "mobile")));
});

test("Forum & Tutorials RBAC: hardware forum only for تعمیرکار, software forum & tutorials only for نرم‌افزارکار", async () => {
  const adminDb = dbFor("admin_uid", ADMIN_EMAIL, true);
  const repairDb = dbFor("repair_uid", "repair@example.com");
  const softwareDb = dbFor("soft_uid", "soft@example.com");
  const sellerDb = dbFor("seller_uid", "seller@example.com");
  const now = new Date(Date.now() - 5000);

  await seedInvitedActiveUser(adminDb, "repair_uid", "repair@example.com", ROLES.REPAIR_TECH);
  await seedInvitedActiveUser(adminDb, "soft_uid", "soft@example.com", ROLES.SOFTWARE_TECH);
  await seedInvitedActiveUser(adminDb, "seller_uid", "seller@example.com", ROLES.ACCESSORY_SELLER);

  const hwPost = (uid) => ({
    board: "hardware",
    flair: "سوال عیبیابی",
    author: "تعمیرکار",
    authorUid: uid,
    authorBadge: ROLES.REPAIR_TECH,
    city: "تهران",
    deviceTag: "iPhone 13",
    pinned: false,
    verifiedSolution: false,
    title: "مشکل بیسباند",
    body: "متن کامل",
    votes: 1,
    upvotedBy: [uid],
    downvotedBy: [],
    savedBy: [],
    createdAtMs: 100000,
    timeLabel: "هماکنون",
    comments: [],
    published: true,
    createdAt: now,
  });

  const swPost = (uid) => ({
    ...hwPost(uid),
    board: "software",
    flair: "FRP و آنلاک",
    author: "نرمافزارکار",
    authorBadge: ROLES.SOFTWARE_TECH,
  });

  // Only تعمیرکار can post/read in hardware forum
  await assertFails(softwareDb.collection("forum_posts").doc("fp_hw_bad").set(hwPost("soft_uid")));
  await assertFails(sellerDb.collection("forum_posts").doc("fp_hw_bad2").set(hwPost("seller_uid")));
  await assertSucceeds(repairDb.collection("forum_posts").doc("fp_hw_1").set(hwPost("repair_uid")));

  // Only نرم‌افزارکار can post/read in software forum
  await assertFails(repairDb.collection("forum_posts").doc("fp_sw_bad").set(swPost("repair_uid")));
  await assertSucceeds(softwareDb.collection("forum_posts").doc("fp_sw_1").set(swPost("soft_uid")));

  // Cross-role reads on restricted boards fail
  await assertFails(softwareDb.collection("forum_posts").doc("fp_hw_1").get());
  await assertFails(repairDb.collection("forum_posts").doc("fp_sw_1").get());
  await assertSucceeds(
    repairDb.collection("forum_posts").where("published", "==", true).where("board", "==", "hardware").get()
  );
  await assertFails(
    softwareDb.collection("forum_posts").where("published", "==", true).where("board", "==", "hardware").get()
  );

  // Tutorials: ONLY نرم‌افزارکار can create or read tutorials
  const tutorialDoc = (uid) => ({
    problemId: "p_frp",
    title: "آموزش جامع بایپس",
    teacher: "استاد نرم‌افزار",
    rating: "۵.۰",
    price: 500000,
    terms: "تضمینی",
    city: "تهران",
    status: "pending",
    ownerId: uid,
    createdAt: now,
  });
  await assertFails(repairDb.collection("tutorials").doc("tut_bad").set(tutorialDoc("repair_uid")));
  await assertSucceeds(softwareDb.collection("tutorials").doc("tut_1").set(tutorialDoc("soft_uid")));
  await assertSucceeds(adminDb.collection("tutorials").doc("tut_1").update({ status: "approved" }));
  await assertFails(repairDb.collection("tutorials").doc("tut_1").get());
  await assertSucceeds(softwareDb.collection("tutorials").doc("tut_1").get());

  // All 7 allowed roles can access general features like hw_ads (mobile/acc/hw), sw_requests, boxes
  await assertSucceeds(
    sellerDb.collection("hw_ads").doc("ad_mob_1").set({
      cat: "موبایل کارکرده",
      title: "آیفون ۱۳ تمیز",
      brand: "Apple",
      model: "iPhone 13",
      series: "CH",
      price: 35000000,
      seller: "فروشنده جانبی",
      phone: "0912",
      city: "تهران",
      province: "تهران",
      region: "همه مناطق",
      ownerId: "seller_uid",
      ownerName: "فروشنده جانبی",
      status: "pending",
      desc: "تمیز",
      section: "mobile",
      sortOrder: 1000,
      createdAt: now,
    })
  );
  await assertSucceeds(
    sellerDb.collection("sw_requests").doc("sr_1").set({
      title: "نیاز به فایل فلش",
      userId: "seller_uid",
      userName: "فروشنده جانبی",
      city: "تهران",
      status: "در انتظار",
      reply: "",
      published: true,
      sortOrder: 1000,
      createdAt: now,
    })
  );
});
