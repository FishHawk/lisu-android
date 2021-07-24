package com.fishhawk.driftinglibraryandroid.ui.more

//abstract class BaseSettingFragment : PreferenceFragmentCompat() {
//    protected abstract val titleResId: Int
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val contentView = super.onCreateView(inflater, container, savedInstanceState)!!
//        val view = ComposeView(requireContext())
//        view.setContent {
//            ApplicationTheme {
//                ProvideWindowInsets {
//                    Scaffold(
//                        topBar = {
//                            TopAppBar(
//                                backgroundColor = MaterialTheme.colors.surface,
//                                contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
//                                title = { Text(stringResource(titleResId)) },
//                                navigationIcon = {
////                                    IconButton(onClick = { findNavController().navigateUp() }) {
////                                        Icon(Icons.Filled.NavigateBefore, "back")
////                                    }
//                                }
//                            )
//                        },
//                        content = {
//                            AndroidView(
//                                modifier = Modifier.fillMaxSize(),
//                                factory = { contentView }
//                            )
//                        }
//                    )
//                }
//            }
//        }
//        return view
//    }
//}